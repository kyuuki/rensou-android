package jp.kyuuki.rensou.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jp.kyuuki.rensou.android.activities.MainActivity
import jp.kyuuki.rensou.android.commons.Logger
import jp.kyuuki.rensou.android.R

class RensouFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = RensouFirebaseMessagingService::class.java.simpleName
        const val CHANNEL_MESSAGE_ID = "channel_message"
    }

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken(token = $token)");

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token)
        RegistrationUtil.sendRegistrationToServer(token, this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val from = remoteMessage.from
        val data = remoteMessage.data

        val message: String? = data["message"]
        Logger.d(TAG, "From: $from")
        Logger.d(TAG, "Message: $message")

        if (message != null) {
            createNotificationChannel()
            sendNotification(message)
        }
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_MESSAGE_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_message_name)
            val descriptionText = getString(R.string.channel_message_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_MESSAGE_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}