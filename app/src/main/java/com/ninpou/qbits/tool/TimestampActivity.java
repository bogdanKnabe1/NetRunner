package com.ninpou.qbits.tool;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimestampActivity extends BaseActivity {
    private static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
    private TextInputEditText contentEdit;
    private Button computeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timestamp);
        setTitle(R.string.title_activity_timestamp);
        contentEdit = findViewById(R.id.et_timestamp);
        computeButton = findViewById(R.id.btn_timestamp);
        initView();
    }

    private void initView() {
        computeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                if (content.isEmpty()) return;
                try {
                    long timestamp = Long.parseLong(content);
                    Date date = new Date(timestamp);
                    contentEdit.setText(dateFormat.format(date));
                } catch (NumberFormatException ignored) {
                }
            }
        });
    }
}
