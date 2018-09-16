package au.com.gridstone.debugdrawer

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import timber.log.Timber

/**
 * This module is designed to be used in conjunction with [LumberYard]; it displays a button that
 * pops a dialog showing [Timber] logs when clicked.
 *
 * If [LumberYard.install] has not been called in [Application.onCreate] then this dialog will not
 * display anything.
 */
class TimberModule : DebugDrawerModule {

  private var dialog: Dialog? = null

  override fun onCreateView(parent: ViewGroup): View {
    val button = Button(parent.context)
    button.setText(R.string.drawer_logsButton)
    button.setOnClickListener {
      val context = ContextThemeWrapper(parent.context, R.style.Theme_DebugDrawer_Dialog)
      dialog = LogsDialog(context).apply { show() }
    }

    return button
  }

  override fun onDetach(context: Context) {
    dialog?.dismiss()
    dialog = null
  }
}
