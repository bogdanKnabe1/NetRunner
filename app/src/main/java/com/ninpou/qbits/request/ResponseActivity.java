package com.ninpou.qbits.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;

import java.util.Objects;


public class ResponseActivity extends BaseActivity {
    private static final String BUNDLE_KEY_RESPONSE = "response_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        ResponseInfo info = (ResponseInfo) getIntent().getSerializableExtra(BUNDLE_KEY_RESPONSE);
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), info);

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }

    public static class ContentFragment extends Fragment {
        private static final String KEY_TEXT = "key_text";

        public ContentFragment() {
        }

        public static ContentFragment newInstance(String text) {
            ContentFragment fragment = new ContentFragment();
            Bundle args = new Bundle();
            args.putString(KEY_TEXT, text);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_response, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView textView = view.findViewById(R.id.tv_response);
            assert getArguments() != null;
            textView.setText(getArguments().getString(KEY_TEXT));
        }
    }


    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ResponseInfo info;

        public SectionsPagerAdapter(FragmentManager fm, ResponseInfo responseInfo) {
            super(fm);
            info = responseInfo;
        }

        @Override
        public Fragment getItem(int position) {
            String text = position == 0 ? info.getHeader() : info.getContent();
            return ContentFragment.newInstance(text);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
