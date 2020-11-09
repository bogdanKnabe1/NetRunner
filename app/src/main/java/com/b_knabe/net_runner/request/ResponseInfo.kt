package com.b_knabe.net_runner.request

import okhttp3.Response
import java.io.Serializable

class ResponseInfo(response: Response) : Serializable {
    var content: String? = null
    val header: String = response.headers().toString()

    init {
        content = try {
            response.body()?.string()
        } catch (e: Exception) {
            "null"
        }
    }
}