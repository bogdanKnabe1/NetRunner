package com.ninpou.packetcapture.core.util.common

import java.io.IOException
import java.util.*

object Shells {
    val dns: String?
        get() {
            var scanner: Scanner? = null
            return try {
                val process = Runtime.getRuntime().exec("getprop net.dns1")
                scanner = Scanner(process.inputStream)
                scanner.nextLine()
            } catch (e: IOException) {
                null
            } finally {
                IOUtils.close(scanner)
            }
        }
}