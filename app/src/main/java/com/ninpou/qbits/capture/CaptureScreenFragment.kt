package com.ninpou.qbits.capture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ninpou.qbits.R

class CaptureScreenFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_screen_capture, container, false)
        initView()
        return rootView
    }

    //set default screen as CaptureFragment
    private fun initView() {
        val transaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.capture_container, CaptureFragment())
        transaction?.show(CaptureFragment())
        transaction?.commit()
    }

    //static instance of MainFragment
    companion object {
        fun newInstance(): CaptureScreenFragment {
            return CaptureScreenFragment()
        }
    }
}