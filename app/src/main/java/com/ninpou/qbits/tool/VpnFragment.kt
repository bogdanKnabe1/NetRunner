package com.ninpou.qbits.tool

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.packetcapture.core.vpn.VpnServiceImpl
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import com.ninpou.qbits.util.hideViews
import com.ninpou.qbits.util.showViews
import kotlinx.android.synthetic.main.fragment_vpn.*
import kotlinx.android.synthetic.main.fragment_vpn.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VpnFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_vpn, container, false)

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_VPN)

        rootView.button_vpn.setOnClickListener {
            showViews(rootView.my_progressBar)
            GlobalScope.launch(Dispatchers.Main) {
                for (a in 2..100) {
                    rootView.my_progressBar.incrementProgressBy(2)
                    delay(20)
                }
                startVpn()
                my_progressBar.progress = 0
            }
        }
        rootView.button_vpn_off.setOnClickListener {
            showViews(rootView.my_progressBar)
            GlobalScope.launch(Dispatchers.Main) {
                for (a in 2..100) {
                    rootView.my_progressBar.incrementProgressBy(2)
                    delay(20)
                }
                stopVpn()
                rootView.my_progressBar.progress = 0
            }
        }
        return rootView
    }

    private fun startVpn() {
        my_progressBar.visibility = View.INVISIBLE
        textViewStatus.text = resources.getString(R.string.vpn_is_on)
        showViews(working_indicator, button_vpn_off)
        hideViews(button_vpn)
        startCapture()
    }

    private fun stopVpn() {
        my_progressBar.progress = 0
        showViews(my_progressBar, button_vpn)
        hideViews(button_vpn_off)
        textViewStatus.text = resources.getString(R.string.vpn_is_off)
        working_indicator.visibility = View.INVISIBLE
        stopCapture()
    }

    private fun startCapture() {
        val intent = VpnService.prepare(APP_ACTIVITY)
        if (intent == null) {
            onActivityResult(0, RESULT_OK, null)
        } else {
            startActivityForResult(intent, 0)
        }
    }

    private fun stopCapture() {
        val intent = Intent(APP_ACTIVITY, VpnServiceImpl::class.java)
        intent.putExtra(KEY_CMD, 1)
        requireActivity().startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val intent = Intent(requireActivity(), VpnServiceImpl::class.java)
            intent.putExtra(KEY_CMD, 0)
            requireActivity().startService(intent)
        }
    }

    //override action bar back button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                fragmentManager?.popBackStackImmediate()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //detach action bar settings
    override fun onDestroyView() {
        super.onDestroyView()
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setTitle(R.string.app_name)
        setHasOptionsMenu(false)
    }

    companion object {
        private const val KEY_CMD = "key_cmd"
    }
}