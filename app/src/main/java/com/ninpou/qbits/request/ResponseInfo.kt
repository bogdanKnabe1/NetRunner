package com.ninpou.qbits.request

import okhttp3.Response
import java.io.Serializable
import java.util.*

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