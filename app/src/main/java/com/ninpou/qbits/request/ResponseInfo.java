package com.ninpou.qbits.request;

import java.io.Serializable;
import java.util.Objects;

import okhttp3.Response;

public class ResponseInfo implements Serializable {
    private String content;
    private String header;

    public ResponseInfo(Response response) {
        header = response.headers().toString();
        try {
            content = Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            content = "null";
        }
    }

    public String getHeader() {
        return header;
    }

    public String getContent() {
        return content;
    }
}
