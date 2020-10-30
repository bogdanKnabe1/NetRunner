package com.ninpou.qbits.request

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ninpou.qbits.R
import com.ninpou.qbits.util.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.android.synthetic.main.fragment_request.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.util.*

class RequestFragment : Fragment() {
    private var client = OkHttpClient()
    private val headers: MutableList<TextInputLayout> = ArrayList()
    private var progressDialog: ProgressDialog? = null
    private var method = HttpMethod.GET
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_request, container, false)
        initView(rootView)
        return rootView
    }

    private fun initView(root: View) {
        progressDialog = ProgressDialog(APP_ACTIVITY)
        progressDialog?.setMessage(getString(R.string.load))
        progressDialog?.setCanceledOnTouchOutside(false)
        val userAgent: String? = try {
            WebSettings.getDefaultUserAgent(APP_ACTIVITY)
        } catch (e: Exception) {
            System.getProperty(KEY_USER_AGENT)
        }
        root.et_user_agent.setText(userAgent)
        root.et_content_type.setText(DEFAULT_CONTENT_TYPE)
        root.button_send.setOnClickListener { sendRequest() }
        root.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                method = HttpMethod.values()[position]
                if (method == HttpMethod.GET) {
                    group_body.visibility = View.INVISIBLE
                } else {
                    showViews(group_body)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        root.btn_add.setOnClickListener { addHeader() }
        root.btn_reset.setOnClickListener {
            for (view in headers) {
                ll_headers.removeView(view)
            }
            headers.clear()
        }
    }

    private fun addHeader() {
        val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.alert_add_header, null)
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_alert_title)
                .setView(view)
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { _, _ ->
                    val editText = view.findViewById<EditText>(R.id.et_add_header)
                    val key = editText.text.toString()
                    if (key.isEmpty()) return@OnClickListener
                    val layout = TextInputLayout(requireContext())
                    val header = TextInputEditText(requireContext())
                    header.hint = key
                    layout.addView(header)
                    ll_headers.addView(layout)
                    headers.add(layout)
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun initRequest(): Request? {
        val url = et_url.text.toString()
        val userAgent = et_user_agent.text.toString()
        val contentType = et_content_type.text.toString()
        if (url.isEmpty()) {
            showShortToast(getString(R.string.url_empty_tip))
            return null
        }
        if (!url.matches(URL_REGEX.toRegex())) {
            showShortToast(getString(R.string.url_illegal_tip))
            return null
        }
        val builder = Request.Builder()
        builder.url(url)
        try {
            if (userAgent.isNotEmpty()) builder.addHeader(HEADER_KEY_USER_AGENT, userAgent)
            if (contentType.isNotEmpty()) builder.addHeader(HEADER_KEY_CONTENT_TYPE, contentType)
            for (header in headers) {
                if (header.editText == null || header.editText?.text == null || header.editText?.text.toString().isEmpty()) continue
                val editText = header.editText
                builder.addHeader(editText?.hint.toString(), editText?.text.toString())
            }
        } catch (e: IllegalArgumentException) {
            showShortToast(getString(R.string.illegal_header_tip))
            return null
        }
        val bodyStr = Objects.requireNonNull(et_body.text).toString()
        val body = RequestBody.create(MediaType.parse(contentType), bodyStr)
        when (method) {
            HttpMethod.GET -> builder.get()
            HttpMethod.POST -> builder.post(body)
            HttpMethod.HEAD -> builder.head()
            HttpMethod.PUT -> builder.put(body)
            HttpMethod.DELETE -> builder.delete(body)
        }
        return builder.build()
    }

    private fun sendRequest() {
        val request = initRequest() ?: return
        progressDialog?.show()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val message = e.message
                GlobalScope.launch(Dispatchers.Main) {
                    progressDialog?.cancel()
                    showShortToast(getString(R.string.request_fail_tip))
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                GlobalScope.launch(Dispatchers.Main) {
                    val intent = Intent(APP_ACTIVITY, ResponseActivity::class.java)
                    val info = ResponseInfo(response)
                    progressDialog?.cancel()
                    intent.putExtra(BUNDLE_KEY_RESPONSE, info)
                    startActivity(intent)
                }
            }
        })
    }

    private enum class HttpMethod {
        GET, POST, HEAD, PUT, DELETE
    }

    companion object {
        fun newInstance(): RequestFragment {
            return RequestFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog = null
    }
}

