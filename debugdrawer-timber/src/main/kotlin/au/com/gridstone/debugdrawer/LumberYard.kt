package au.com.gridstone.debugdrawer

import android.app.Application
import android.os.AsyncTask
import android.os.Handler
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Calendar
import java.util.Deque
import java.util.Locale

/**
 * Keep track of [Timber] logs that can be displayed by [TimberModule]. In order for logs to be
 * collected [LumberYard.install] must be called inside of [Application.onCreate].
 *
 * A no-op version of `LumberYard` is provided too, meaning you can keep the same
 * `LumberYard.install()` call inside of `onCreate()` for release builds.
 */
object LumberYard {

  private const val BUFFER_SIZE = 200

  private val entries: Deque<Entry> = ArrayDeque(BUFFER_SIZE + 1)

  private lateinit var app: Application
  private lateinit var handler: Handler
  private var listener: ((Entry) -> Unit)? = null

  /**
   * Installs `LumberYard` for the app, planting a [DebugTree] that [TimberModule] can use to
   * display logs from the debug drawer.
   */
  fun install(app: Application) {
    this.app = app
    handler = Handler(app.mainLooper)

    CleanUpTask.execute()

    Timber.plant(object : DebugTree() {
      override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val entry = Entry(priority, tag, message)
        addEntry(entry)

        if (listener != null) {
          handler.post { listener?.invoke(entry) }
        }
      }
    })
  }

  internal fun getEntries(): List<Entry> = entries.toList()

  internal fun setListener(listener: (Entry) -> Unit) {
    assertInitialised()
    this.listener = listener
  }

  internal fun clearListener() {
    assertInitialised()
    listener = null
    handler.removeCallbacksAndMessages(null)
  }

  internal fun save(callback: (File?) -> Unit) {
    assertInitialised()
    SaveTask(callback).execute()
  }

  @Synchronized private fun addEntry(entry: Entry) {
    entries.addLast(entry)
    if (entries.size > BUFFER_SIZE) entries.removeFirst()
  }

  private fun assertInitialised() {
    if (!LumberYard::app.isInitialized) {
      throw IllegalStateException("LumberYard.install() has not been called.")
    }
  }

  private class SaveTask(private val callback: (File?) -> Unit) : AsyncTask<Unit, Unit, File?>() {
    override fun doInBackground(vararg params: Unit?): File? {
      val folder: File = app.getExternalFilesDir(null) ?: return null

      val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
      val timestamp: String = formatter.format(Calendar.getInstance().time)
      val filename = "$timestamp.log"

      val file = File(folder, filename)
      val bufferedEntries = entries.toList()

      @Suppress("LiftReturnOrAssignment") // Keep things readable.
      try {
        val writer = BufferedWriter(FileWriter(file))

        for (entry in bufferedEntries) {
          writer.write(entry.prettyPrint())
          writer.newLine()
        }

        writer.close()

        return file
      } catch (e: IOException) {
        return null
      }
    }

    override fun onPostExecute(result: File?) {
      callback(result)
    }
  }

  private object CleanUpTask : AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg params: Unit?) {
      val folder: File? = app.getExternalFilesDir(null)
      val files: Array<File>? = folder?.listFiles()
      files?.filter { it.name.endsWith(".log") }?.forEach { it.delete() }
    }
  }
}
