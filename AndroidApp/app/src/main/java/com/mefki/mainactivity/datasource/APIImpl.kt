package com.mefki.mainactivity.datasource

import com.mefki.mainactivity.datamodel.StationBikes
import com.mefki.mainactivity.datamodel.StationLoc
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class APIImpl : API{
    private val baseUrl: String = "http://35.204.87.5/"

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var api: API = retrofit.create(API::class.java)

    override fun getStationsLocalizations(): Observable<List<StationLoc>> {
        return api.getStationsLocalizations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getAvailableBikes(vararg ids: Int): Observable<List<StationBikes>> {
        return api.getAvailableBikes(*ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}