package jp.naist.ubi_lab.androidsample.view.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.widget.TextView
import jp.naist.ubi_lab.androidsample.R
import jp.naist.ubi_lab.androidsample.model.SensorData

class SensorViewAdapter : BaseAdapter() {
    private val tag = "SensorViewAdapter"
    private lateinit var context: Context
    private lateinit var layoutInflater: LayoutInflater
    lateinit var sensorList: ArrayList<SensorData>

    fun sensorViewAdapter(context: Context){
        this.context = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val convertView = layoutInflater.inflate(R.layout.sensor_utils, parent, false)
        (convertView.findViewById(R.id.sensor_time) as TextView).text = sensorList[position].time
        (convertView.findViewById(R.id.text_acc_x) as TextView).text = String.format("%1$.4f",sensorList[position].acc_x)
        (convertView.findViewById(R.id.text_acc_y) as TextView).text = String.format("%1$.4f",sensorList[position].acc_y)
        (convertView.findViewById(R.id.text_acc_z) as TextView).text = String.format("%1$.4f",sensorList[position].acc_z)
        (convertView.findViewById(R.id.text_mag_x) as TextView).text = String.format("%1$.4f",sensorList[position].mag_x)
        (convertView.findViewById(R.id.text_mag_y) as TextView).text = String.format("%1$.4f",sensorList[position].mag_y)
        (convertView.findViewById(R.id.text_mag_z) as TextView).text = String.format("%1$.4f",sensorList[position].mag_z)

        return convertView
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        Log.e(tag,"Item changed")
    }

    override fun getItem(position: Int): Any {
        return sensorList[position]
    }

    override fun getItemId(position: Int): Long {
        return sensorList[position].id!!
    }

    override fun getCount(): Int {
        return sensorList.size
    }
}
