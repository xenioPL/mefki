package com.mefki.mainactivity.datamodel

import com.google.gson.annotations.SerializedName

data class StationLoc(
    @SerializedName("id")  val id: Int,
    @SerializedName("lon") val longitude: Float,
    @SerializedName("lat") val latitude: Float
)