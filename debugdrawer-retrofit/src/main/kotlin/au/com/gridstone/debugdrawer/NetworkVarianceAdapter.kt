package au.com.gridstone.debugdrawer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class NetworkVarianceAdapter(context: Context) : BindableAdapter<Int>(context) {

  private val values = intArrayOf(20, 40, 60)

  fun getPositionForValue(value: Int): Int {
    val position = values.indexOf(value)
    if (position >= 0) return position
    return 1 // Default to 40% if something changes.
  }

  override fun getCount(): Int = values.size

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getItem(position: Int): Int = values[position]

  override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View =
      inflater.inflate(android.R.layout.simple_spinner_item, container, false)

  override fun bindView(item: Int, position: Int, view: View) {
    val textView = view.findViewById<TextView>(android.R.id.text1)
    textView.text = view.context.getString(R.string.drawer_retrofitVarianceEntry, item)
  }

  override fun newDropDownView(inflater: LayoutInflater, position: Int,
                               container: ViewGroup): View =
      inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
}
