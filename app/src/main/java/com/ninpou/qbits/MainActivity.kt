package com.ninpou.qbits

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ninpou.qbits.capture.CaptureFragment
import com.ninpou.qbits.capture.CaptureScreenFragment
import com.ninpou.qbits.capture.PacketDetailFragment
import com.ninpou.qbits.request.RequestFragment
import com.ninpou.qbits.util.APP_ACTIVITY


class MainActivity : BaseActivity(), CaptureFragment.DataPassListener {
    private val fragments = arrayOf(
            CaptureScreenFragment.newInstance(),
            RequestFragment.newInstance(),
            MainFragment.newInstance()
    )
    private var lastFragmentIndex = 0
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_capture -> {
                switchFragment(0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_request -> {
                switchFragment(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tool -> {
                switchFragment(2)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        APP_ACTIVITY = this //getting link of current activity

        initView()
        val actionBar = supportActionBar
        actionBar?.title = "NetRunner"
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initView() {
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pager, fragments[0])
        transaction.show(fragments[0])
        transaction.commit()
    }

    private fun switchFragment(index: Int) {
        if (index == lastFragmentIndex) return
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.hide(fragments[lastFragmentIndex])
        if (!fragments[index].isAdded) {
            transaction.add(R.id.pager, fragments[index])
        }
        transaction.show(fragments[index])
        lastFragmentIndex = index
        transaction.commitAllowingStateLoss()
    }

    // new method for passData between fragments, with help of activity MAIN
    override fun passData(data: String?) {
        val fragment: Fragment = PacketDetailFragment()
        val args = Bundle()
        args.putString(PacketDetailFragment.DATA_RECEIVE, data)
        fragment.arguments = args
        replaceFragment(fragment)
    }

    //replace mechanism inside capture container
    private fun replaceFragment(someFragment: Fragment) {
        val transaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
        transaction?.replace(R.id.capture_container, someFragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
    }

}