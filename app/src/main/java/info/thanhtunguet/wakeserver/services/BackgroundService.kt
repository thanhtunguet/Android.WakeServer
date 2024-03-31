package info.thanhtunguet.wakeserver.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import info.thanhtunguet.wakeserver.repositories.ClientRepository
import info.thanhtunguet.wakeserver.BuildConfig
import info.thanhtunguet.wakeserver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class BackgroundService : Service() {
    companion object {
        private const val TAG = "AutoIP"

        private const val DURATION: Long = 120000L

        const val CHANNEL_NAME = "AutoIP"

        const val CHANNEL_ID = 1
    }

    private var webServer: WebServer? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    private val handler: Handler = Handler(
        Looper.getMainLooper()
    )

    private val ipCheckerRunnable = IpCheckerRunnable()

    inner class IpCheckerRunnable() : Runnable {
        override fun run() {
            scope.launch {
                setForegroundNotification(
                    "IP checking",
                    "Checking for current public IP..."
                )
                Log.d(TAG, "Checking for current public IP...")
                val publicIP = ClientRepository.currentIpAddress().trim()
                Log.d(TAG, "Current public IP is $publicIP")
                Log.d(TAG, "Checking for current IP setting...")
                val currentIP = ClientRepository.getDNS()
                Log.d(TAG, "Current IP setting is $currentIP")

                if (currentIP != publicIP) {
                    ClientRepository.setDNS(publicIP)
                    ClientRepository.sendChat(
                        "Current public IP is $publicIP != $currentIP and has been updated"
                    )
                } else {
                    Log.d(TAG, "Current public IP is up to date")
                }
                setForegroundNotification(
                    "IP is up to date",
                    "Current public IP is $publicIP"
                )
                handler.postDelayed(this@IpCheckerRunnable, DURATION)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWebServer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWebServer()
    }

    private fun startWebServer() {
        webServer = WebServer(this, BuildConfig.WEB_PORT)
        try {
            webServer!!.start()
            Log.i("WebServer", "Web server is running on port ${BuildConfig.WEB_PORT}")
        } catch (ioe: IOException) {
            Log.e("WebServer", "The server could not start.", ioe)
        }
    }

    private fun stopWebServer() {
        if (webServer != null) {
            webServer!!.stop()
            Log.i("WebServer", "Web server stopped.")
        }
    }

    override fun onCreate() {
        super.onCreate()
        setForegroundNotification(
            "AutoIP is enabled",
            "Your public IP is up to date",
        )
        ipCheckerRunnable.run()
    }

    private fun setForegroundNotification(title: String, content: String) {
        Log.d(TAG, "$title: $content")
        val channel = NotificationChannel(
            CHANNEL_NAME,
            "AUTO_IP_NOTIFICATION_CHANNEL",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_NAME)
            .setSmallIcon(R.drawable.cloudflare_icon)
            .setContentTitle(title)
            .setContentText(content)
            .build()
        startForeground(CHANNEL_ID, notification)
    }
}
