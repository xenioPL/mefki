package com.mefki.mainactivity.datasource

import com.mefki.mainactivity.datamodel.StationBikes
import com.mefki.mainactivity.datamodel.StationLoc
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface API{
    @GET("getStations")
    fun getStationsLocalizations(): Observable<List<StationLoc>>

    @GET("getNumberFromStations")
    fun getAvailableBikes(@Query("stations", encoded = false) ids: String): Observable<List<StationBikes>>
}