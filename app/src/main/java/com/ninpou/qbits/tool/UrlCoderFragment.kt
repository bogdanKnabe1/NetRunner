package com.ninpou.qbits.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.qbits.MainActivity
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import kotlinx.android.synthetic.main.fragment_url_coder.*
import kotlinx.android.synthetic.main.fragment_url_coder.view.*
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class UrlCoderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_url_coder, container, false)

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_activity_url)
        initView(rootView)
        return rootView
    }

    private fun initView(root: View) {
        //CHANGED encode and decode -- TEST
        root.btn_code.setOnClickListener(View.OnClickListener {
            val content = et_code.text.toString()
            if (content.isEmpty()) return@OnClickListener
            try {
                et_code.setText(URLEncoder.encode(content, StandardCharsets.UTF_8.toString()))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        })
        root.btn_decode.setOnClickListener(View.OnClickListener {
            val content = et_code.text.toString()
            if (content.isEmpty()) return@OnClickListener
            try {
                et_code.setText(URLDecoder.decode(content, StandardCharsets.UTF_8.toString()))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setTitle(R.string.app_name)
        setHasOptionsMenu(false)
    }
}