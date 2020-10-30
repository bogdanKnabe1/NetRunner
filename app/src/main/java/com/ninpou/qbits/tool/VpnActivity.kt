package com.ninpou.qbits.tool

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ninpou.packetcapture.core.vpn.VpnEventHandler.getInstance
import com.ninpou.packetcapture.core.vpn.VpnServiceImpl
import com.ninpou.qbits.R
import com.ninpou.qbits.util.hideViews
import com.ninpou.qbits.util.showViews
import kotlinx.android.synthetic.main.activity_vpn.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VpnActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpn)

        button_vpn.setOnClickListener {
            showViews(my_progressBar)
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
            showViews(my_progressBar)
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
        showViews(working_indicator,button_vpn_off)
        hideViews(button_vpn)
        startCapture()
    }

    private fun stopVpn() {
        my_progressBar.progress = 0
        my_progressBar.visibility = View.INVISIBLE
        textViewStatus.text = resources.getString(R.string.vpn_is_off)
        working_indicator.visibility = View.INVISIBLE
        showViews(button_vpn)
        hideViews(button_vpn_off)
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

    companion object {
        private const val KEY_CMD = "key_cmd"
    }
}