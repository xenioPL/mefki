package com.mefki.mainactivity.datamodel

import com.google.gson.annotations.SerializedName

data class StationBikes(
    @SerializedName("id")  val id: Int,
    @SerializedName("number") val bikesAvailable: Int
)