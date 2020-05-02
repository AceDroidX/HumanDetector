package io.github.acedroidx.humandetector

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.nio.charset.StandardCharsets
import java.util.*


class BluetoothService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    var btdevice: BluetoothDevice? = null
    var btsocket: BluetoothSocket? = null
    //private var state: Boolean = false

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                }
                if (bluetoothAdapter?.isEnabled == false) {
                    toast("蓝牙未开启");return
                }
                if (msg.obj !is Intent) {
                    toast("未知错误1");return
                }
                if ((msg.obj as Intent).extras == null) {
                    toast("未知错误2");return
                }
                val btname = (msg.obj as Intent).extras?.get("name")
                val btmac = (msg.obj as Intent).extras?.get("mac")
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    if (btname == deviceName && btmac == deviceHardwareAddress) {
                        btdevice = device
                        return@forEach
                    }
                }
                if (btdevice == null) {
                    toast("找不到连接的设备");return
                }
                toast("get btdevice")
                ///////////////////connect
                bluetoothAdapter?.cancelDiscovery()
                if (btsocket != null) if (btsocket!!.isConnected) btsocket!!.close();Thread.sleep(
                    1000
                )
                btsocket =
                    btdevice?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                toast("开始连接")
                Log.d("text", "connecting")
                btsocket?.connect()
                Log.d("text", "connect success")
                toast("连接成功")
                btsocket?.outputStream?.write("ping".toByteArray())
                while (true) {
                    if (!btsocket?.isConnected!!) return
                    if (btsocket?.inputStream?.available()!! > 0) {
                        Thread.sleep(100)
                        val buffer = ByteArray(btsocket?.inputStream?.available()!!)
                        btsocket?.inputStream?.read(buffer)
                        toast(String(buffer, StandardCharsets.UTF_8))
                        Log.d("test", String(buffer, StandardCharsets.UTF_8))
                    }
                    Thread.sleep(100)
                }
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
                Toast.makeText(this@BluetoothService, "error:$e", Toast.LENGTH_SHORT).show()
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
            if (intent != null) {
                msg.obj = intent
            }
            serviceHandler?.sendMessage(msg)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        if (btsocket != null) btsocket!!.close()
        stopForeground(true)
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    fun toast(str: String) {
        Handler(Looper.getMainLooper()).post {
            Log.d("toast-btservice",str)
            Toast.makeText(this@BluetoothService, str, Toast.LENGTH_SHORT).show()
        }
    }
}
