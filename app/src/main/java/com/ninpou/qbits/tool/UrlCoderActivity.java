package com.ninpou.qbits.tool;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;

import java.net.URLDecoder;
import java.net.URLEncoder;


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

    //CHECK deprecated
    private void initView() {
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                contentEdit.setText(URLEncoder.encode(content));
            }
        });
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                contentEdit.setText(URLDecoder.decode(content));
            }
        });
    }
}
