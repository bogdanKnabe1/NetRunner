package com.ninpou.qbits.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import kotlinx.android.synthetic.main.fragment_ping.*
import kotlinx.android.synthetic.main.fragment_ping.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class PingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_ping, container, false)
        initView(rootView)

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_activity_ping)
        return rootView
    }

    private fun initView(root: View) {
        root.btn_ping.setOnClickListener {
            val host = et_ping.text.toString()
            tv_ping_result.text = ""
            et_ping.isEnabled = false
            btn_ping.isEnabled = false
            //use coroutine to start ping in background
            GlobalScope.launch {
                ping(host)
            }
        }
    }

    private fun ping(host: String) {
        val cmd = String.format(Locale.getDefault(),
                "ping -c %d %s", PING_COUNT, host)
        try {
            val process = Runtime.getRuntime().exec(cmd)
            val scanner = Scanner(process.inputStream)
            while (scanner.hasNextLine()) {
                val next = scanner.nextLine()
                //dispatch new UI state from coroutine
                GlobalScope.launch(Dispatchers.Main) {
                    val last = """
                        ${tv_ping_result.text}
                        
                        """.trimIndent()
                    tv_ping_result.text = last + next
                    delay(5)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //dispatch new UI state from coroutine
        GlobalScope.launch(Dispatchers.Main) {
            et_ping.isEnabled = true
            btn_ping.isEnabled = true
            delay(5)
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
        private const val PING_COUNT = 10
    }
}