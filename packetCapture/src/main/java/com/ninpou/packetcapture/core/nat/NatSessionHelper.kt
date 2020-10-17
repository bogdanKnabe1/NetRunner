package com.ninpou.packetcapture.core.nat

import com.ninpou.packetcapture.core.util.processparse.PortHostService.Companion.instance
import com.ninpou.packetcapture.core.util.common.ACache
import com.ninpou.packetcapture.core.util.common.FileManager.deleteUnder
import com.ninpou.packetcapture.core.util.common.TimeFormatter.formatToYYMMDDHHMMSS
import com.ninpou.packetcapture.core.util.net.TcpDataSaver
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.startTime
import java.io.File
import java.util.*

object NatSessionHelper {
    @JvmStatic
    val allSessions: Collection<NatSession>
        get() {
            val file = File(TcpDataSaver.CONFIG_DIR
                    + formatToYYMMDDHHMMSS(startTime))
            val aCache = ACache.get(file)
            val list = file.list()
            val baseNetSessions = ArrayList<NatSession>()
            if (list != null) {
                for (fileName in list) {
                    val netConnection = aCache.getAsObject(fileName) as NatSession
                    baseNetSessions.add(netConnection)
                }
            }
            val portHostService = instance
            if (portHostService != null) {
                val aliveConnInfo = portHostService.andRefreshSessionInfo
                baseNetSessions.addAll(aliveConnInfo)
            }
            baseNetSessions.sortWith(Comparator { o1, o2 -> o2.refreshTime.compareTo(o1.refreshTime) })
            return baseNetSessions
        }

    @JvmStatic
    fun clearCache() {
        val data = TcpDataSaver.DATA_DIR
        val config = TcpDataSaver.CONFIG_DIR
        val dataDir = File(data)
        val configDir = File(config)
        deleteUnder(dataDir)
        deleteUnder(configDir)
        NatSessionManager.clearAllSession()
    }
}