package com.ninpou.qbits.tool

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.ninpou.qbits.MainActivity
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import kotlinx.android.synthetic.main.fragment_base64_coder.*
import kotlinx.android.synthetic.main.fragment_base64_coder.view.*

class Base64CoderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_base64_coder, container, false)

        //Get current action bar from main activity and attach settings to action bar in fragment
        val actionBar = APP_ACTIVITY.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.title_activity_base64)
        initView(rootView)
        return rootView
    }

    private fun initView(root: View) {
        root.btn_64code.setOnClickListener(View.OnClickListener {
            val content = et_64code.text.toString()
            if (content.isEmpty()) return@OnClickListener
            val coded = Base64.encodeToString(content.toByteArray(), Base64.DEFAULT)
            et_64code.setText(coded)
        })
        root.btn_64decode.setOnClickListener(View.OnClickListener {
            val content = et_64code.text.toString()
            if (content.isEmpty()) return@OnClickListener
            val decoded = String(Base64.decode(content, Base64.DEFAULT))
            et_64code.setText(decoded)
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