package com.mefki.mainactivity.searchservice

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import com.mefki.mainactivity.MainActivity
import com.mefki.mainactivity.datasource.API
import com.mefki.mainactivity.datasource.APIImpl
import io.reactivex.disposables.Disposable
import android.location.Location
import com.mefki.mainactivity.R
import com.mefki.mainactivity.datamodel.StationLoc
import java.util.ArrayList


class SearchService: Service(){
    private var notificationManager: NotificationManager? = null

    private val NOTIFICATION = R.string.search_service_notification_name

    inner class LocalBinder : Binder() {
        internal val service: SearchService
            get() = this@SearchService
    }

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showNotification(getString(R.string.search_service_notification_title))
    }

    private var myRadius: Int? = null
    private lateinit var myloc: Location
    private lateinit var stationss: List<StationLoc>
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val extras = intent.extras
        myRadius = extras!!.getInt("radius")
        val lon = extras!!.getDouble("lon")
        val lat = extras!!.getDouble("lat")
        val mylocc = Location("")
        mylocc.latitude = lat
        mylocc.longitude = lon
        myloc = mylocc
        api.getStationsLocalizations().subscribe{ stations->
            stationss = stations
            val handler = Handler()
            val runnable = object: Runnable{
                override fun run() {
                    if(!waitingForResponse) {
                        checkStationsForBikes()
                        waitingForResponse = true
                    }
                    if(!bikeFound) {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
            runnable.run()
        }

        return Service.START_STICKY
    }

    private var api: API = APIImpl()
    private lateinit var disposable: Disposable
    private var waitingForResponse = false
    private var bikeFound = false
    private fun checkStationsForBikes(){
        val list = ArrayList<Int>()
        list.add(2)

        for(station in stationss){
            val loc2 = Location("")
            loc2.latitude = station.latitude.toDouble()
            loc2.longitude = station.longitude.toDouble()
            val distance = myloc.distanceTo(loc2)
            if(distance < myRadius!!){
                list.add(station.id)
            }
        }
        val maplist = list.map { it}.toIntArray()
        var string = ""
        for(id in maplist){
            string+=id.toString()
            string+=","
        }
        string = string.dropLast(1)
        disposable = api.getAvailableBikes(string).subscribe{bikes->
            for(bike in bikes){
                if(bike.bikesAvailable > 0){
                    bikeFound = true
                    showNotification("Found a bike!")
                    break
                }
            }
        }
        waitingForResponse = false

    }

    override fun onDestroy() {
        notificationManager!!.cancel(NOTIFICATION)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private val mBinder = LocalBinder()

    private fun showNotification(title: String) {
        val channelID = getString(R.string.search_service_notification_channel_id)
        val smallIcon = R.drawable.ic_logo
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round
        )
        val priority = NotificationCompat.PRIORITY_DEFAULT

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationCompat = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(smallIcon)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setPriority(priority)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()

        val notificationID = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.search_service_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            notificationManager.notify(notificationID, notificationCompat)
        }
        else{
            val notificationManagerCompat = NotificationManagerCompat.from(this)
            notificationManagerCompat.notify(notificationID, notificationCompat)
        }
    }
}