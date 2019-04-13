package com.mefki.mainactivity.alarms

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mefki.mainactivity.R
import com.mefki.mainactivity.datasource.Alarm
import kotlinx.android.synthetic.main.item_alarm.view.*

class AlarmsAdapter(private val resources: Resources, private var alarmsList: ArrayList<Alarm> = arrayListOf()): RecyclerView.Adapter<AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view =  LayoutInflater.from(parent.context).
            inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun getItemCount(): Int {
        return alarmsList.size
    }

    override fun onBindViewHolder(viewHolder: AlarmViewHolder, position: Int) {
        val item = alarmsList[position]
        viewHolder.itemView.item_alarm_switch.text = item.name
        viewHolder.itemView.item_alarm_switch.isChecked = item.isSwitchedOn
        viewHolder.itemView.item_alarm_radius.text = resources.getString(R.string.item_alarm_radius, item.radius)
        viewHolder.itemView.item_alarm_start_time.text = item.timeStart.toString()
        viewHolder.itemView.item_alarm_end_time.text = item.timeEnd.toString()
        viewHolder.itemView.item_alarm_days.text = item.getDaysString()
        viewHolder.itemView.item_alarm_street.text = item.street
    }

    fun setItems(items: ArrayList<Alarm>){
        alarmsList = items
        notifyDataSetChanged()
    }
}