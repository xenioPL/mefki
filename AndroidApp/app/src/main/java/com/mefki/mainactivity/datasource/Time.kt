package com.mefki.mainactivity.datasource

import java.lang.StringBuilder

class Time(private val hours: Int, private val minutes: Int){
    override fun toString(): String {
        val stringBuilder = StringBuilder("")
        if(hours <= 9) stringBuilder.append(0)
        stringBuilder.append(hours)
        stringBuilder.append(":")
        if(minutes <= 9) stringBuilder.append(0)
        stringBuilder.append(minutes)
        return stringBuilder.toString()
    }
}