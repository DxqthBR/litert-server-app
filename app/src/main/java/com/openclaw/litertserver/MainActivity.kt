package com.openclaw.litertserver

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {
    private lateinit var server: LlmServer
    private lateinit var inference: LiteRTInference
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logView = findViewById<TextView>(R.id.log_text)
        val ipView = findViewById<TextView>(R.id.ip_text)

        ipView.text = "IP: ${getIPAddress()}"
        logView.text = "Starting Server...\n"

        inference = LiteRTInference(this)
        server = LlmServer(inference)

        scope.launch(Dispatchers.IO) {
            try {
                // In a real app, the model path would be configurable
                // inference.initialize("/sdcard/model.bin")
                server.start(8080)
                withContext(Dispatchers.Main) {
                    logView.append("Server started on port 8080\n")
                    logView.append("Ready for requests!\n")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logView.append("Error: ${e.message}\n")
                }
            }
        }
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses.toList()
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is InetAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr.indexOf(':') < 0) return sAddr
                    }
                }
            }
        } catch (e: Exception) { }
        return "Unknown"
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        inference.close()
        scope.cancel()
    }
}
