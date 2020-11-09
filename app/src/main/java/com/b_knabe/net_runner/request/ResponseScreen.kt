package com.b_knabe.net_runner.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.b_knabe.net_runner.BuildConfig
import com.b_knabe.net_runner.R

class ResponseScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response)
        val info = intent.getSerializableExtra(BUNDLE_KEY_RESPONSE) as ResponseInfo?
        val pagerAdapter = SectionsPagerAdapter(supportFragmentManager, info)

        // Set up the ViewPager with the sections adapter.
        val viewPager = findViewById<ViewPager>(R.id.container)
        viewPager.adapter = pagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
    }

    class ContentFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_response, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val textView = view.findViewById<TextView>(R.id.tv_response)
            if (BuildConfig.DEBUG && arguments == null) {
                error("Assertion failed")
            }
            textView.text = arguments?.getString(KEY_TEXT)
        }

        companion object {
            private const val KEY_TEXT = "key_text"
            fun newInstance(text: String?): ContentFragment {
                val fragment = ContentFragment()
                val args = Bundle()
                args.putString(KEY_TEXT, text)
                fragment.arguments = args
                return fragment
            }
        }
    }

    class SectionsPagerAdapter(fm: FragmentManager, private val info: ResponseInfo?) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            val text = if (position == 0) info!!.header else info!!.content!!
            return ContentFragment.newInstance(text)
        }

        override fun getCount(): Int {
            return 2
        }
    }

    companion object {
        private const val BUNDLE_KEY_RESPONSE = "response_key"
    }
}