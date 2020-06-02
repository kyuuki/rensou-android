package jp.kyuuki.rensou.android.services

import com.google.firebase.messaging.FirebaseMessagingService
import jp.kyuuki.rensou.android.commons.Logger

class RensouFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = RensouFirebaseMessagingService::class.java.simpleName
    }

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken(token = $token)");

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token)
        RegistrationUtil.sendRegistrationToServer(token, this)
    }
}