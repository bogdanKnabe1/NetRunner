package com.b_knabe.net_runner.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

//changed from java( type casting test )
object Md5Hash {
    fun hash(string: String): String? {
        val hash: ByteArray
        hash = try {
            MessageDigest.getInstance("MD5").digest(string.toByteArray(StandardCharsets.UTF_8))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b and  (0xFF.toByte()) < 0x10) hex.append("0")
            hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
        }
        return hex.toString()
    }
}