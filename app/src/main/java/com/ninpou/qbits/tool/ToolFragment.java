package com.ninpou.qbits.tool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ninpou.qbits.R;

public class ToolFragment extends Fragment {

    public ToolFragment() {
        // Required empty public constructor
    }

    public static ToolFragment newInstance() {
        return new ToolFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tool, container, false);
    }
}