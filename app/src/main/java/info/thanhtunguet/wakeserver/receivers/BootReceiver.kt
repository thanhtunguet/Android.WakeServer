package info.thanhtunguet.wakeserver.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.thanhtunguet.wakeserver.MainActivity


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, MainActivity::class.java)
            serviceIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(serviceIntent)
        }
    }
}
