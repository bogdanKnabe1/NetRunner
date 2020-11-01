package com.ninpou.qbits.tool

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import com.ninpou.qbits.util.setActionBarFragment
import com.ninpou.qbits.util.setDefaultActionBar
import kotlinx.android.synthetic.main.fragment_base64_coder.*
import kotlinx.android.synthetic.main.fragment_base64_coder.view.*

/*
* Base64 is a way to encode arbitrary binary data into ASCII text.
* At its core, coding is very simple. Every six bits of the input are encoded into one of the characters in the 64-letter alphabet.
* The “standard” alphabet used for this is A-Z, a-z, 0-9, +, / and = as the padding character at the end. So there are 4 characters for every 3 bytes of data.
* */

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
        setActionBarFragment(getString(R.string.title_fragment_base64))
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
        setDefaultActionBar()
        setHasOptionsMenu(false)
    }
}