package com.mefki.mainactivity.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mefki.mainactivity.R
import com.mefki.mainactivity.datasource.Alarm
import com.mefki.mainactivity.datasource.Day
import com.mefki.mainactivity.datasource.Time
import kotlinx.android.synthetic.main.fragment_alarms.view.*

class AlarmsFragment: Fragment(){

    private lateinit var mView: View
    private lateinit var adapter: AlarmsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_alarms, container, false)

        initList()
        adapter.setItems(getTestData())

        return mView
    }

    private fun initList(){
        adapter = AlarmsAdapter(resources)
        mView.alarms_recycler_view.adapter = adapter
        mView.alarms_recycler_view.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun getTestData(): ArrayList<Alarm>{
        val days = HashMap<Day, Boolean>()
        days[Day.MON] = true
        days[Day.WED] = true

        return arrayListOf(
            Alarm("Alarm 1", false, days, "street", 100, 2, Time(11,0), Time(12,0)),
            Alarm("Alarm 2", true, days, "street 2", 130, 1, Time(8,0), Time(9,0))
        )
    }
}
