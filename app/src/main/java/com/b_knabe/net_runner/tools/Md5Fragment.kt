package com.b_knabe.net_runner.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.b_knabe.net_runner.R
import com.b_knabe.net_runner.util.Md5Hash
import com.b_knabe.net_runner.util.setActionBarFragment
import com.b_knabe.net_runner.util.setDefaultActionBar
import kotlinx.android.synthetic.main.fragment_md5.*
import kotlinx.android.synthetic.main.fragment_md5.view.*

/*
* MD5 is a 128-bit hashing algorithm designed to create "fingerprints"
* or digests of messages of arbitrary length and then verify their authenticity.
* It is necessary in order to understand whether the file has changed during the download,
* and it is the same as on the server from which we downloaded.
*/
class Md5Fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_md5, container, false)
        initView(rootView)

        //Get current action bar from main activity and attach settings to action bar in fragment
        setActionBarFragment(getString(R.string.title_fragment_md5))
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
        setDefaultActionBar()
        setHasOptionsMenu(false)
    }
}