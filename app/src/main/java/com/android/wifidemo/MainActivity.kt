package com.android.wifidemo


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

//wifi config
private const val ssid = "SmartBed"
private const val pwd = "1234567890"

//socket config
const val ipAddress = "192.168.4.1" // 要连接的设备的 IP 地址
const val port = 7890 // 要连接的设备的端口号
const val message = "Hello, device!" // 要发送的消息
class MainActivity : AppCompatActivity() {

    private var connecting: Boolean = false
    private val deviceInfo = MutableLiveData("start")
    private val btnLink: Button by lazy { findViewById(R.id.btnLink) }
    private val btnDisconnect: AppCompatButton by lazy { findViewById(R.id.btnDisconnect) }
    private val number: TextView by lazy { findViewById(R.id.number) }
    private val btnSocket: Button by lazy { findViewById(R.id.btnSocket) }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        XXPermissions.with(this)
            // 申请单个权限
            .permission(Permission.ACCESS_FINE_LOCATION)
            .request { _, allGranted ->
                if (!allGranted) {
                    Toaster.show("获取部分权限成功，但部分权限未正常授予")
                }
            }

        btnLink.setOnClickListener {
            WifiSupport.connect(ssid, pwd, this)
        }
        btnSocket.setOnClickListener {


            establishSocketConnection {
                // 更新LiveData
                deviceInfo.value = it
                Log.e(" deviceInfo.value", deviceInfo.value.toString())
            }

        }

        btnDisconnect.setOnClickListener {
            connecting = false
        }

        deviceInfo.observe(this) {
            // 在这里更新你的 UI
            // it 是当前的设备信息
            number.text = it
        }
    }

    private fun establishSocketConnection(handleMessage: (String?) -> Unit) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val address = InetAddress.getByName(ipAddress)
                val socket = Socket(address, port)
                connecting = true
                val output = PrintWriter(socket.outputStream, true)
                // 发送数据到 WiFi 设备
                output.println(message)
                val outputStream = socket.getOutputStream()
                outputStream.write(message.toByteArray())

                // 持续接收 WiFi 设备发送的数据
                try {
                    while (connecting) {
                        val inputStream = socket.getInputStream()
                        val buffer = ByteArray(1024)
                        val bytesRead = inputStream.read(buffer)
                        val response = String(buffer, 0, bytesRead)
                        withContext(Dispatchers.Main) {
                            handleMessage(response)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    socket.close()
                }
            }
        }
    }
}



