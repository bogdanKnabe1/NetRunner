package com.ninpou.packetcapture.core.util.common

object StringUtil {
    fun getSocketSize(size: Long): String {
        val showSum: String = when {
            size > 1000000 -> {
                (size / 1000000.0 + 0.5).toInt().toString()+"mb"
            }
            size > 1000 -> {
                (size / 1000.0 + 0.5).toInt().toString()+"kb"
            }
            else -> {
                size.toString() + "b"
            }
        }
        return showSum
    }
}