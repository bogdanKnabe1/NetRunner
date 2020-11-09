package com.b_knabe.net_runner.tools

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.b_knabe.packet_capture.core.vpn.VpnServiceImpl
import com.b_knabe.net_runner.R
import com.b_knabe.net_runner.util.*
import kotlinx.android.synthetic.main.fragment_vpn.*
import kotlinx.android.synthetic.main.fragment_vpn.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
-- VPN
* a generic name for technologies that allow one or more network connections (logical network) to be provided over another network (for example, the Internet).
* Despite the fact that communications are carried out over networks with a lower or unknown level of trust (for example, over public networks),
* the level of trust in the constructed logical network does not depend on the level of trust in the underlying networks
* due to the use of cryptography tools (encryption, authentication, public key infrastructure,
* means for protection against repetitions and changes of messages transmitted over the logical network).

    CAN be modified in VPNServiceImp
*/
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
        setActionBarFragment(getString(R.string.title_VPN))

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
        setDefaultActionBar()
        setHasOptionsMenu(false)
    }

    companion object {
        private const val KEY_CMD = "key_cmd"
    }
}