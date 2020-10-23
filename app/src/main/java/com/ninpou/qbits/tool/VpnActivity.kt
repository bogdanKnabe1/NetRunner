package com.ninpou.qbits.tool

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ninpou.packetcapture.core.vpn.VpnEvent.getInstance
import com.ninpou.packetcapture.core.vpn.VpnServiceImpl
import com.ninpou.qbits.R
import kotlinx.android.synthetic.main.activity_vpn.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class VpnActivity : AppCompatActivity() {
    private val KEY_CMD = "key_cmd"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpn)

        button_vpn.setOnClickListener {
            my_progressBar.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                for (a in 2..100) {
                    my_progressBar.incrementProgressBy(2)
                    delay(20)
                }
                startVpn()
                my_progressBar.progress = 0
            }
        }
        button_vpn_off.setOnClickListener {
            my_progressBar.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                for (a in 2..100) {
                    my_progressBar.incrementProgressBy(2)
                    delay(20)
                }
                stopVpn()
                my_progressBar.progress = 0
            }
        }
    }

    private fun startVpn() {
        my_progressBar.visibility = View.INVISIBLE
        textViewStatus.text = resources.getString(R.string.vpn_is_on)
        working_indicator.visibility = View.VISIBLE
        button_vpn.visibility = View.GONE
        button_vpn_off.visibility = View.VISIBLE
        startCapture()
    }

    private fun stopVpn() {
        my_progressBar.progress = 0
        my_progressBar.visibility = View.INVISIBLE
        textViewStatus.text = resources.getString(R.string.vpn_is_off)
        working_indicator.visibility = View.INVISIBLE
        button_vpn.visibility = View.VISIBLE
        button_vpn_off.visibility = View.GONE
        stopCapture()
    }

    private fun startCapture() {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            onActivityResult(0, RESULT_OK, null)
        } else {
            startActivityForResult(intent, 0)
        }
    }

    private fun stopCapture() {
        val intent = Intent(this, VpnServiceImpl::class.java)
        intent.putExtra(KEY_CMD, 1)
        this.startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val intent = Intent(this, VpnServiceImpl::class.java)
            intent.putExtra(KEY_CMD, 0)
            this.startService(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        getInstance().cancelAll()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}