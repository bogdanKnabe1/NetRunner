package com.b_knabe.net_runner.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.b_knabe.net_runner.R
import com.b_knabe.net_runner.util.setActionBarFragment
import com.b_knabe.net_runner.util.setDefaultActionBar
import kotlinx.android.synthetic.main.fragment_url_coder.*
import kotlinx.android.synthetic.main.fragment_url_coder.view.*
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/*
  -- ENCODE
* When URL encoding a string, the following rules apply:
* The alphanumeric characters (a-z, A-Z, and 0-9) remain the same.
* The special characters ., -, *, and _ remain the same.
*
* The white space character " " is converted into a + sign.
* This is opposite to other programming languages like JavaScript which encodes the space character into %20.
* But it is completely valid as the spaces in query string parameters are represented by +, and not %20.
* The %20 is generally used to represent spaces in URI itself (the URL part before ?).
*
* All other characters are considered unsafe and are first converted into one or more bytes using the given encoding scheme.
* Then each byte is represented by the 3-character string %XY, where XY is the two-digit hexadecimal representation of the byte.
*/

/*
 -- DECODE
* URL decoding is the process of converting URL encoding query strings and form parameters into their original form. By default,
* HTML form parameters are encoded using application/x-www-form-urlencoded MIME type.
* Before using them in your application, you must decode them. The same is the case with query string parameters included in the URL.
* Mostly, these parameters are already decoded by the framework you're using in your application like Spring or Express.
* But in a standalone Java application, you must manually decode query string and form parameters by using the URLDecoder utility class.
* */

class UrlCoderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_url_coder, container, false)
        initView(rootView)
        //Get current action bar from main activity and attach settings to action bar in fragment
        setActionBarFragment(getString(R.string.title_fragment_url))
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
        setDefaultActionBar()
        setHasOptionsMenu(false)
    }
}