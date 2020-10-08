package com.ninpou.qbits.tool;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.ninpou.qbits.R;


public class ToolFragment extends Fragment {
    private CardView urlCard;
    private CardView base64Card;
    private CardView md5Card;
    private CardView timestampCard;
    private CardView pingCard;

    public static ToolFragment newInstance() {
        return new ToolFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tool, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View root) {
        urlCard = root.findViewById(R.id.card_url_coder);
        base64Card = root.findViewById(R.id.card_base64);
        md5Card = root.findViewById(R.id.card_md5);
        timestampCard = root.findViewById(R.id.card_timestamp);
        pingCard = root.findViewById(R.id.card_ping);
        urlCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UrlCoderActivity.class);
                startActivity(intent);
            }
        });
        base64Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Base64CoderActivity.class);
                startActivity(intent);
            }
        });
        md5Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Md5Activity.class);
                startActivity(intent);
            }
        });
        timestampCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TimestampActivity.class);
                startActivity(intent);
            }
        });
        pingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PingActivity.class);
                startActivity(intent);
            }
        });
    }
}
