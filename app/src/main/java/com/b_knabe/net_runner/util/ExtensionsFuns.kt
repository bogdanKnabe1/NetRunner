package com.b_knabe.net_runner.util

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.b_knabe.net_runner.R

fun showShortToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

fun switchFragment(index: Int, fragments: Array<Fragment>) {
    if (index == lastFragmentIndex) return
    val transaction: FragmentTransaction = APP_ACTIVITY.supportFragmentManager.beginTransaction()
    transaction.hide(fragments[lastFragmentIndex])
    if (!fragments[index].isAdded) {
        transaction.add(R.id.nav_host_fragment, fragments[index])
    }
    transaction.show(fragments[index])
    lastFragmentIndex = index
    transaction.commitAllowingStateLoss()
}

fun setActionBar(title:String) {
    val actionBar = APP_ACTIVITY.supportActionBar
    actionBar?.setDisplayShowHomeEnabled(true)
    actionBar?.title = title
}

fun setActionBarFragment(title: String){
    val actionBar = APP_ACTIVITY.supportActionBar
    actionBar?.title = title
    actionBar?.setDisplayHomeAsUpEnabled(true)
}

fun setDefaultActionBar(){
    val actionBar = APP_ACTIVITY.supportActionBar
    actionBar?.setDisplayHomeAsUpEnabled(false)
    actionBar?.setTitle(R.string.app_name)
}

fun View.hideViews() {
    visibility = View.GONE
}

fun hideViews(vararg views: View) {
    views.forEach { it.hideViews() }
}

fun View.showViews() {
    visibility = View.VISIBLE
}

fun showViews(vararg views: View) {
    views.forEach { it.showViews() }
}