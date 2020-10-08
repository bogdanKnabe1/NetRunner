package com.ninpou.qbits.tool;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;


public class Base64CoderActivity extends BaseActivity {
    private TextInputEditText contentEdit;
    private Button codeButton;
    private Button decodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base64_coder);
        setTitle(R.string.title_activity_base64);
        contentEdit = findViewById(R.id.et_64code);
        codeButton = findViewById(R.id.btn_64code);
        decodeButton = findViewById(R.id.btn_64decode);
        initView();
    }

    private void initView() {
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                String coded = Base64.encodeToString(content.getBytes(), Base64.DEFAULT);
                contentEdit.setText(coded);
            }
        });
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                String decoded = new String(Base64.decode(content, Base64.DEFAULT));
                contentEdit.setText(decoded);
            }
        });
    }
}
