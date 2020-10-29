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
import com.ninpou.qbits.util.Md5Hash
import kotlinx.android.synthetic.main.fragment_md5.*
import kotlinx.android.synthetic.main.fragment_md5.view.*

class Md5Fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_md5, container, false)
        initView(rootView)

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_activity_md5)
        return rootView
    }

    private fun initView(root: View) {
        root.btn_compute.setOnClickListener(View.OnClickListener {
            val content = et_md5.text.toString()
            if (content.isEmpty()) return@OnClickListener
            et_md5.setText(Md5Hash.hash(content))
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

    //detach action bar settings
    override fun onDestroyView() {
        super.onDestroyView()
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setTitle(R.string.app_name)
        setHasOptionsMenu(false)
    }
}