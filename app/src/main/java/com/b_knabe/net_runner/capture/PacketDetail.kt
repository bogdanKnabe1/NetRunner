package com.b_knabe.net_runner.capture

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.b_knabe.packet_capture.core.util.net_utils.tcp.TcpDataLoader
import com.b_knabe.net_runner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

// Packet information screen
class PacketDetail : AppCompatActivity() {
    private var requestTextView: TextView? = null
    private var responseTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packet_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    private fun initView() {
        requestTextView = findViewById(R.id.tv_req_content)
        responseTextView = findViewById(R.id.tv_rsp_content)
        val dir = intent.getStringExtra(KEY_DIR)
        object : Thread() {
            override fun run() {
                val file = File(dir)
                val files = file.listFiles() ?: return
                Arrays.sort(files) { o1, o2 -> o1.lastModified().compareTo(o2.lastModified()) }
                for (item in files) {
                    val data = TcpDataLoader.loadSaveFile(item) ?: continue
                    if (data.isRequest) {
                        setRequest(data.headStr + data.bodyStr)
                    } else {
                        setResponse(data.headStr + data.bodyStr)
                    }
                }
            }
        }.start()
    }

    private fun setRequest(content: String) {
        GlobalScope.launch(Dispatchers.Main) {
            requestTextView?.text = content
        }
    }

    private fun setResponse(content: String) {
        GlobalScope.launch(Dispatchers.Main) {
            responseTextView?.text = content
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        private const val KEY_DIR = "key_dir"
    }
}