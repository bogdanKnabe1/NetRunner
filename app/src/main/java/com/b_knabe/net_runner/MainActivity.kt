package com.b_knabe.net_runner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.b_knabe.net_runner.capture.CaptureFragment
import com.b_knabe.net_runner.request.RequestFragment
import com.b_knabe.net_runner.tools.MainFragment
import com.b_knabe.net_runner.util.APP_ACTIVITY
import com.b_knabe.net_runner.util.setActionBar
import com.b_knabe.net_runner.util.switchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val fragments = arrayOf(
            CaptureFragment.newInstance(),
            RequestFragment.newInstance(),
            MainFragment.newInstance()
    )

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_capture_ui -> {
                switchFragment(0, fragments)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_request_ui -> {
                switchFragment(1, fragments)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tool_ui -> {
                switchFragment(2, fragments)
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
        setActionBar(getString(R.string.app_name))
    }

    fun initView() {
        val navigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in_main, R.anim.fade_out_main)
        transaction.replace(R.id.nav_host_fragment, fragments[0])
        transaction.show(fragments[0])
        transaction.commit()
    }

    //For testing
    fun initFragment(position: Int){
        val transaction: FragmentTransaction = APP_ACTIVITY.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fade_in_main, R.anim.fade_out_main)

        when (position){
            0 -> captureFragment(transaction)
            1 -> requestFragment(transaction)
            2 -> mainFragment(transaction)
        }
    }

    private fun captureFragment(transaction: FragmentTransaction){
        transaction.replace(R.id.nav_host_fragment, fragments[0])
        transaction.show(fragments[0])
        transaction.commit()
    }

    private fun requestFragment(transaction: FragmentTransaction){
        transaction.replace(R.id.nav_host_fragment, fragments[1])
        transaction.show(fragments[1])
        transaction.commit()
    }

    private fun mainFragment(transaction: FragmentTransaction){
        transaction.replace(R.id.nav_host_fragment, fragments[2])
        transaction.show(fragments[2])
        transaction.commit()
    }
}