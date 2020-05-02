package io.github.acedroidx.humandetector

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.lang.Process

class BluetoothService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    //private var state: Boolean = false

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000)
                Toast.makeText(this@BluetoothService, "service sleep", Toast.LENGTH_SHORT).show()
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()
            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        val notification: Notification = NotificationCompat.Builder(this, "bt_bg")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getText(R.string.channel_name))
            .setContentText(getText(R.string.channel_description))
            //.setTicker("test")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification);
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        stopForeground(true)
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}
