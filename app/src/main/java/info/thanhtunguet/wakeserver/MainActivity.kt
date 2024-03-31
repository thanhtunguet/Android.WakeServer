package info.thanhtunguet.wakeserver

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import info.thanhtunguet.wakeserver.services.BackgroundService
import info.thanhtunguet.wakeserver.ui.theme.WakeServerTheme


class MainActivity : ComponentActivity() {
    companion object {
        val Tag
            get() = this::class.simpleName

        const val NEW_IP_ACTION = "info.thanhtunguet.NEW_IP_ACTION"

        const val TARGET_APP_ACTION = "info.thanhtunguet.ACTION_START_TARGET_APP"
    }

    private var wakeLock: WakeLock? = null

    override fun onDestroy() {
        super.onDestroy()
        wakeLock!!.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WakeServer:WakeLock",
        )

        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        this.wakeLock = wakeLock

        setContent {
            WakeServerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("WakeServer is started")
                }
            }
        }
        startBackgroundService()
    }

    private fun startBackgroundService() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        val isRunning = isBackgroundServiceRunning(this)
        if (!isRunning) {
            startService(serviceIntent)
        }
    }

    private fun isBackgroundServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.className == BackgroundService::class.java.name) {
                return true
            }
        }
        return false
    }
}

@Composable
fun Greeting(name: String) {
    /// modifier: Modifier = Modifier
    Text(
        text = name,
        modifier = Modifier
            .fillMaxSize() // Make the Text composable fill the available space
            .wrapContentSize(Alignment.Center) // Align the content (text) to the center
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WakeServerTheme {
        Greeting("Android")
    }
}
