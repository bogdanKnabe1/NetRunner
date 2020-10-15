package com.ninpou.packetcapture.core.util.common

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

//Locale fix for inconsistent state
object TimeFormatter {
    private val HHMMSSSFormat: DateFormat = SimpleDateFormat("HH:mm:ss:s", Locale.getDefault())
    private val formatYYMMDDHHMMSSFormat: DateFormat = SimpleDateFormat("yyyy:MM:dd-HH:mm:ss:s", Locale.getDefault())

    @JvmStatic
    fun formatToHHMMSSMM(time: Long): String {
        val date = Date(time)
        return HHMMSSSFormat.format(date)
    }

    @JvmStatic
    fun formatToYYMMDDHHMMSS(time: Long): String {
        val date = Date(time)
        return formatYYMMDDHHMMSSFormat.format(date)
    }
}