package io.github.acedroidx.humandetector

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var btdevice:BluetoothDevice?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createBGNotificationChannel()
        createWarningNotificationChannel()
    }

    private fun createBGNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel("bt_bg", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createWarningNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "警报"
            val descriptionText = "人体检测警报"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("warning", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    public fun setupBT(view: View) {
        var listView = findViewById<ListView>(R.id.bt_devices_list)
        if(listView.visibility==VISIBLE){
            listView.visibility=GONE
            return
        }
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
        val array = ArrayList<String>()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.d("test", deviceName)
            array.add(deviceName+"    "+deviceHardwareAddress)
        }
        var btdevicetext=findViewById<TextView>(R.id.btdevice_text)
        listView.setAdapter(
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                array
            )
        )
        listView.setOnItemClickListener { parent, view, position, id ->
            var result = parent.getItemAtPosition (position).toString();//获取选择项的值
            //Toast.makeText(this, "您点击了" + result, Toast.LENGTH_SHORT).show();
            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                if (deviceName+"    "+deviceHardwareAddress==result){
                    btdevice=device
                    listView.visibility=GONE
                    btdevicetext.text=deviceName
                    return@forEach
                }
            }
        }
        listView.visibility=VISIBLE
    }

    public fun startBGService(view: View) {
        if (btdevice==null){
            Toast.makeText(this, "请先选择要连接的蓝牙设备", Toast.LENGTH_SHORT).show()
            return
        }
        var intent=Intent(this, BluetoothService::class.java)
        intent.putExtra("name", btdevice!!.name)
        intent.putExtra("mac", btdevice!!.address)
        val bgservice = startService(intent)
    }

    public fun stopBGService(view: View) {
        val bgservice = Intent(this, BluetoothService::class.java).also { intent ->
            stopService(intent)
        }
    }
}
