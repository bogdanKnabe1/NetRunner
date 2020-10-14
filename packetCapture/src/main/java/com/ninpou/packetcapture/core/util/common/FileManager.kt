package com.ninpou.packetcapture.core.util.common

import java.io.File

object FileManager {
    private fun deleteFile(file: File, reserveSelf: Boolean) {
        if (!file.exists()) return
        if (file.isDirectory) {
            val files = file.listFiles()
            for (child in files) {
                delete(child)
            }
        }
        if (!reserveSelf) file.delete()
    }

    fun delete(file: File) {
        deleteFile(file, false)
    }

    @JvmStatic
    fun deleteUnder(file: File) {
        deleteFile(file, true)
    }
}