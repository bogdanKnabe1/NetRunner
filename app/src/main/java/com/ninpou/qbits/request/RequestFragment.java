package com.ninpou.qbits.request;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ninpou.qbits.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import top.srsea.lever.ui.Toasts;


public class RequestFragment extends Fragment {
    private static final String URL_REGEX = "(http|https)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    private static final String KEY_USER_AGENT = "http.agent";
    private static final String HEADER_KEY_USER_AGENT = "User-Agent";
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String BUNDLE_KEY_RESPONSE = "response_key";

    private OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler();
    private List<TextInputLayout> headers = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FloatingActionButton sendButton;
    private Group bodyGroup;
    private TextInputEditText bodyEdit;
    private TextInputEditText urlEdit;
    private Spinner functionSpinner;
    private TextInputEditText userAgentEdit;
    private TextInputEditText contentTypeEdit;
    private LinearLayout headersLayout;
    private TextView resetButton;
    private TextView addButton;
    private HttpMethod method = HttpMethod.GET;

    public static RequestFragment newInstance() {
        return new RequestFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_request, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View root) {
        sendButton = root.findViewById(R.id.button_send);
        urlEdit = root.findViewById(R.id.et_url);
        functionSpinner = root.findViewById(R.id.spinner);
        userAgentEdit = root.findViewById(R.id.et_user_agent);
        contentTypeEdit = root.findViewById(R.id.et_content_type);
        bodyGroup = root.findViewById(R.id.group_body);
        bodyEdit = root.findViewById(R.id.et_body);
        addButton = root.findViewById(R.id.btn_add);
        resetButton = root.findViewById(R.id.btn_reset);
        headersLayout = root.findViewById(R.id.ll_headers);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.load));
        progressDialog.setCanceledOnTouchOutside(false);
        String userAgent;
        try {
            userAgent = WebSettings.getDefaultUserAgent(getActivity());
        } catch (Exception e) {
            userAgent = System.getProperty(KEY_USER_AGENT);
        }
        userAgentEdit.setText(userAgent);
        contentTypeEdit.setText(DEFAULT_CONTENT_TYPE);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        functionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                method = HttpMethod.values()[position];
                if (method == HttpMethod.GET) {
                    bodyGroup.setVisibility(View.INVISIBLE);
                } else {
                    bodyGroup.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHeader();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (View view : headers) {
                    headersLayout.removeView(view);
                }
                headers.clear();
            }
        });
    }

    private void addHeader() {
        final View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.alert_add_header, null);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_alert_title)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = view.findViewById(R.id.et_add_header);
                        String key = editText.getText().toString();
                        if (key.isEmpty()) return;
                        TextInputLayout layout = new TextInputLayout(requireContext());
                        TextInputEditText header = new TextInputEditText(requireContext());
                        header.setHint(key);
                        layout.addView(header);
                        headersLayout.addView(layout);
                        headers.add(layout);
                    }
                })
                .setNegativeButton(R.string.cancle, null)
                .show();
    }

    private Request initRequest() {
        String url = urlEdit.getText().toString();
        String userAgent = userAgentEdit.getText().toString();
        String contentType = contentTypeEdit.getText().toString();
        if (url.isEmpty()) {
            Toasts.of(R.string.url_empty_tip).showShort();
            return null;
        }
        if (!url.matches(URL_REGEX)) {
            Toasts.of(R.string.url_illegal_tip).showShort();
            return null;
        }
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        try {
            if (!userAgent.isEmpty())
                builder.addHeader(HEADER_KEY_USER_AGENT, userAgent);
            if (!contentType.isEmpty())
                builder.addHeader(HEADER_KEY_CONTENT_TYPE, contentType);
            for (TextInputLayout header : headers) {
                if (header.getEditText() == null || header.getEditText().getText() == null
                        || header.getEditText().getText().toString().isEmpty())
                    continue;
                EditText editText = header.getEditText();
                builder.addHeader(editText.getHint().toString(), editText.getText().toString());
            }
        } catch (IllegalArgumentException e) {
            Toasts.of(R.string.illegal_header_tip).showShort();
            return null;
        }
        String bodyStr = bodyEdit.getText().toString();
        RequestBody body = RequestBody.create(MediaType.parse(contentType), bodyStr);
        switch (method) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(body);
                break;
            case HEAD:
                builder.head();
                break;
            case PUT:
                builder.put(body);
                break;
            case DELETE:
                builder.delete(body);
                break;
        }
        return builder.build();
    }

    private void sendRequest() {
        Request request = initRequest();
        if (request == null) return;
        progressDialog.show();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                final String message = e.getMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                        Toasts.of(R.string.request_fail_tip).showShort();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response)
                    throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), ResponseActivity.class);
                        ResponseInfo info = new ResponseInfo(response);
                        progressDialog.cancel();
                        intent.putExtra(BUNDLE_KEY_RESPONSE, info);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private enum HttpMethod {
        GET, POST, HEAD, PUT, DELETE
    }
}
