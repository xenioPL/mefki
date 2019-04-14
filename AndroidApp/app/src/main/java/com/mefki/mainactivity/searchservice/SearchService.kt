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
import com.mefki.mainactivity.R
import android.app.PendingIntent
import com.mefki.mainactivity.MainActivity

class SearchService: Service(){
    private var notificationManager: NotificationManager? = null

    private val NOTIFICATION = R.string.search_service_notification_name

    inner class LocalBinder : Binder() {
        internal val service: SearchService
            get() = this@SearchService
    }

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val handler = Handler()
        val runnable = object: Runnable{
            override fun run() {
                handler.postDelayed(this, 1000)
            }
        }
        runnable.run()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        notificationManager!!.cancel(NOTIFICATION)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private val mBinder = LocalBinder()

    private fun showNotification() {
        val channelID = getString(R.string.search_service_notification_channel_id)
        val smallIcon = R.drawable.ic_logo
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round
        )
        val contentTitle = getString(R.string.search_service_notification_title)
        val contentText = getString(R.string.search_service_notification_description)
        val priority = NotificationCompat.PRIORITY_DEFAULT

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationCompat = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(smallIcon)
            .setLargeIcon(largeIcon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
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