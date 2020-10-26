package com.ninpou.qbits.capture

import android.os.Bundle
import android.widget.TextView
import com.ninpou.packetcapture.core.util.net_utils.TcpDataLoader
import com.ninpou.qbits.BaseActivity
import com.ninpou.qbits.R
import java.io.File
import java.util.*

class PacketDetailActivity : BaseActivity() {
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
                Arrays.sort(files) { o1, o2 -> java.lang.Long.compare(o1.lastModified(), o2.lastModified()) }
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
        runOnUiThread { requestTextView!!.text = content }
    }

    private fun setResponse(content: String) {
        runOnUiThread { responseTextView!!.text = content }
    }

    companion object {
        private const val KEY_DIR = "key_dir"
    }
}