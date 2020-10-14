package com.ninpou.qbits.tool;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class UrlCoderActivity extends BaseActivity {
    private TextInputEditText contentEdit;
    private Button codeButton;
    private Button decodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_coder);
        setTitle(R.string.title_activity_url);
        contentEdit = findViewById(R.id.et_code);
        codeButton = findViewById(R.id.btn_code);
        decodeButton = findViewById(R.id.btn_decode);
        initView();
    }

    private void initView() {
        //CHANGED encode and decode -- TEST
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                try {
                    contentEdit.setText(URLEncoder.encode(content, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                try {
                    contentEdit.setText(URLDecoder.decode(content, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
