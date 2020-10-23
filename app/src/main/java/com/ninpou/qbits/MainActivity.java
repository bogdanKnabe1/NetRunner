package com.ninpou.qbits;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninpou.qbits.capture.CaptureFragment;
import com.ninpou.qbits.request.RequestFragment;
import com.ninpou.qbits.tool.ToolFragment;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private final Fragment[] fragments = new Fragment[]{
            CaptureFragment.newInstance(),
            RequestFragment.newInstance(),
            ToolFragment.newInstance()
    };
    private int lastFragmentIndex = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_capture:
                    switchFragment(0);
                    return true;
                case R.id.navigation_request:
                    switchFragment(1);
                    return true;
                case R.id.navigation_tool:
                    switchFragment(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Netrunner");
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        //actionBar.setIcon(R.mipmap.ic_launcher_new);

    }

    private void initView() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        FragmentTransaction transaction;
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.pager, fragments[0]);
        transaction.show(fragments[0]);
        transaction.commit();
    }

    private void switchFragment(int index) {
        if (index == lastFragmentIndex) return;
        FragmentTransaction transaction;
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments[lastFragmentIndex]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.pager, fragments[index]);
        }
        transaction.show(fragments[index]);
        lastFragmentIndex = index;
        transaction.commitAllowingStateLoss();
    }

}
