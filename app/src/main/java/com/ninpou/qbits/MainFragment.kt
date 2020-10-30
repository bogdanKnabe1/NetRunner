package com.ninpou.qbits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ninpou.qbits.tool.ToolFragment

// MAIN container for tools
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        initView()
        return rootView
    }

    //set default screen as ToolsFragment
    private fun initView() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, ToolFragment())
        transaction.show(ToolFragment())
        transaction.commit()
    }

    //static instance of MainFragment
    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}