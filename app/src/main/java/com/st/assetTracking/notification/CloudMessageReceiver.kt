package com.st.assetTracking.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.st.assetTracking.MainAssetTracking
import com.st.assetTracking.dashboard.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class CloudMessageReceiver : FirebaseMessagingService() {
    override fun onNewToken(tocken: String) {
        Log.d("NEW_TOKEN", tocken)
        storeNotificationToken(this,tocken)
        val tokenRegistration = TokenRegistrationUtility(this)
        runBlocking(Dispatchers.IO) {
            tokenRegistration.registerWithSNS(tocken)
        }
    }

    /**
     * get the logo to display in the notificaiton, if present it will use the app logo, otherwise the
     * ic_dialog_alert icon
     * @return icon to use in the notificaiton
     */
    @DrawableRes
    private fun getResourceLogo(): Int {
        val packageName = packageName
        @DrawableRes var logo: Int = R.drawable.sensor_acc_event_free_fall
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            if (applicationInfo.logo != 0) logo = applicationInfo.logo
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return logo
    }

    private fun createContentIntent():PendingIntent{
        return PendingIntent.getActivity(this, START_DASHBOARD_ACTIVITY_REQ,
                Intent(this, MainAssetTracking::class.java),FLAG_ONE_SHOT)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService<NotificationManager>() ?: return
        val params = remoteMessage.data
        val messageContent = JSONObject(params as Map<*, *>)
        Log.e("JSON_OBJECT", messageContent.toString())

        buildNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
                //.setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setContentTitle(getString(R.string.cloudMessage_notificationTitle))
                .setContentText(messageContent.toString())
                .setSmallIcon(getResourceLogo())
                .setContentIntent(createContentIntent())
                .setAutoCancel(true)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun buildNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 1000, 500, 1000)
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.cloudMessage_channelName),
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = getString(R.string.cloudMessage_channelDesc)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = pattern
            notificationChannel.enableVibration(true)
            notificationChannel.canBypassDnd()//// to display notification in DND Mode
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object{
        private const val NOTIFICATION_CHANNEL_ID = "dashboard_notification"
        private const val NOTIFICATION_ID = 1
        private const val START_DASHBOARD_ACTIVITY_REQ = 1
        private val TOKEN_KEY = CloudMessageReceiver::class.java.name+".TOKEN_KEY"

        private fun storeNotificationToken(context: Context,token:String){
            val preferences = context.getSharedPreferences(CloudMessageReceiver::class.java.name,Context.MODE_PRIVATE)
            preferences.edit {
                putString(TOKEN_KEY,token)
            }
        }

        fun getNotificationToken(context: Context):String?{
            val preferences = context.getSharedPreferences(CloudMessageReceiver::class.java.name,Context.MODE_PRIVATE)
            return preferences.getString(TOKEN_KEY,null)
        }
    }
}