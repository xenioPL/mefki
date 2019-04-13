package com.mefki.mainactivity.datasource

import java.lang.StringBuilder

class Alarm(val name: String,
            val isSwitchedOn: Boolean,
            val days: HashMap<Day, Boolean>,
            val street: String,
            val radius: Int,
            val numberOfBikes: Int,
            val timeStart: Time,
            val timeEnd: Time){

    fun getDaysString(): String{
        val string = StringBuilder("")
        for(day in Day.values()){
            if(days[day] == true) {
                string.append(day.name)
                string.append(", ")
            }
        }
        string.delete(string.length - 2, string.length - 1)
        return string.toString()
    }
}