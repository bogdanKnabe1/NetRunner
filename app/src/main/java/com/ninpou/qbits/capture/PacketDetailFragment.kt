package com.ninpou.qbits.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.packetcapture.core.util.net_utils.TcpDataLoader
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import kotlinx.android.synthetic.main.fragment_packet_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

//change to fragment and get data through Bundle args = getArguments()
class PacketDetailFragment : Fragment() {

    private var args: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        args = arguments
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_packet_detail, container, false)
        initView()

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_fragment_packet)
        return rootView
    }

    private fun initView() {
        /*val dir = intent.getStringExtra(KEY_DIR)*/
        object : Thread() {
            override fun run() {
                val dir: String = args?.getString(DATA_RECEIVE) ?: " "
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
        GlobalScope.launch(Dispatchers.Main) { tv_req_content.text = content }
    }

    private fun setResponse(content: String) {
        GlobalScope.launch(Dispatchers.Main) { tv_rsp_content.text = content }
    }

    //override action bar back button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                fragmentManager?.popBackStack()
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
        private const val KEY_DIR = "key_dir"
        const val DATA_RECEIVE = "data_receive"
    }
}