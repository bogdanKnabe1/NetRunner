package com.ninpou.qbits.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ninpou.qbits.R
import com.ninpou.qbits.util.APP_ACTIVITY
import com.ninpou.qbits.util.setActionBarFragment
import com.ninpou.qbits.util.setDefaultActionBar
import kotlinx.android.synthetic.main.fragment_timestamp.view.*
import java.text.SimpleDateFormat
import java.util.*

/* Timestamp it is a sequence of characters or encoded information indicating when a particular event occurred.
 * Usually displays the date and time (sometimes accurate to a fraction of a second).
 * */
class TimestampFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //for action bar
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_timestamp, container, false)
        initView(rootView)

        //Get current action bar from main activity and attach settings to action bar in fragment
        setActionBarFragment(getString(R.string.title_fragment_timestamp))
        return rootView
    }

    //init all view's
    private fun initView(rootView: View) {
        rootView.btn_timestamp.setOnClickListener(View.OnClickListener {
            val content = rootView.et_timestamp.text.toString()
            if (content.isEmpty()) return@OnClickListener
            try {
                val timestamp = content.toLong()
                val date = Date(timestamp)
                rootView.et_timestamp.setText(dateFormat.format(date))
            } catch (ignored: NumberFormatException) {
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

    //detach action bar settings
    override fun onDestroyView() {
        super.onDestroyView()
        setDefaultActionBar()
        setHasOptionsMenu(false)
    }

    companion object {
        private val dateFormat = SimpleDateFormat.getDateTimeInstance()
    }
}