package com.ninpou.qbits.capture;

import android.os.Bundle;
import android.widget.TextView;

import com.ninpou.packetcapture.core.util.net_utils.TcpDataLoader;
import com.ninpou.qbits.BaseActivity;
import com.ninpou.qbits.R;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class PacketDetailActivity extends BaseActivity {
    private static final String KEY_DIR = "key_dir";

    private TextView requestTextView;
    private TextView responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_detail);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        initView();
    }

    private void initView() {
        requestTextView = findViewById(R.id.tv_req_content);
        responseTextView = findViewById(R.id.tv_rsp_content);
        final String dir = getIntent().getStringExtra(KEY_DIR);
        new Thread() {
            @Override
            public void run() {
                File file = new File(dir);
                File[] files = file.listFiles();
                if (files == null) return;
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return Long.compare(o1.lastModified(), o2.lastModified());
                    }
                });
                for (File item : files) {
                    TcpDataLoader.TcpData data = TcpDataLoader.loadSaveFile(item);
                    if (data == null) continue;
                    if (data.isRequest()) {
                        setRequest(data.getHeadStr() + data.getBodyStr());
                    } else {
                        setResponse(data.getHeadStr() + data.getBodyStr());
                    }
                }
            }
        }.start();
    }

    private void setRequest(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestTextView.setText(content);
            }
        });
    }

    private void setResponse(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responseTextView.setText(content);
            }
        });
    }
}
