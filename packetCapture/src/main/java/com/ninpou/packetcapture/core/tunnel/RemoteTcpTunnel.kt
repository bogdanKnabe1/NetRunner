package com.ninpou.packetcapture.core.tunnel

import android.os.Handler
import android.os.Looper
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.nat.NatSessionManager
import com.ninpou.packetcapture.core.util.android.PortHostService
import com.ninpou.packetcapture.core.util.common.ACache
import com.ninpou.packetcapture.core.util.common.ThreadPool
import com.ninpou.packetcapture.core.util.common.TimeFormatter.formatToYYMMDDHHMMSS
import com.ninpou.packetcapture.core.util.net.TcpDataSaver
import java.io.File
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Selector

class RemoteTcpTunnel(serverAddress: InetSocketAddress?, selector: Selector?, portKey: Short) : RawTcpTunnel(serverAddress, selector, portKey) {
    private val handler: Handler
    var helper: TcpDataSaver
    var session: NatSession
    @Throws(Exception::class)
    override fun afterReceived(buffer: ByteBuffer?) {
        super.afterReceived(buffer)
        refreshSessionAfterRead(buffer!!.limit())
        val saveTcpData = TcpDataSaver.TcpData.Builder()
                .isRequest(false)
                .needParseData(buffer.array())
                .length(buffer.limit())
                .offSet(0)
                .build()
        helper.addData(saveTcpData)
    }

    @Throws(Exception::class)
    override fun beforeSend(buffer: ByteBuffer?) {
        super.beforeSend(buffer)
        val saveTcpData = TcpDataSaver.TcpData.Builder()
                .isRequest(true)
                .needParseData(buffer!!.array())
                .length(buffer.limit())
                .offSet(0)
                .build()
        helper.addData(saveTcpData)
        refreshAppInfo()
    }

    private fun refreshAppInfo() {
        if (session.appInfo != null) {
            return
        }
        if (PortHostService.instance != null) {
            ThreadPool.instance.execute { PortHostService.instance!!.refreshSessionInfo() }
        }
    }

    private fun refreshSessionAfterRead(size: Int) {
        session.receivePacketNum++
        session.receiveByteNum += size.toLong()
    }

    override fun onDispose() {
        super.onDispose()
        handler.postDelayed(Runnable {
            ThreadPool.instance.execute(Runnable {
                if (session.receiveByteNum == 0L && session.bytesSent == 0) {
                    return@Runnable
                }
                val configFileDir = (TcpDataSaver.CONFIG_DIR
                        + formatToYYMMDDHHMMSS(session.vpnStartTime))
                val parentFile = File(configFileDir)
                if (!parentFile.exists()) {
                    parentFile.mkdirs()
                }
                val file = File(parentFile, session.uniqueName)
                if (file.exists()) {
                    return@Runnable
                }
                val configACache = ACache.get(parentFile)
                configACache.put(session.uniqueName, session)
            })
        }, 1000)
    }

    init {
        //? null assert !!
        session = NatSessionManager.getSession(portKey)!!
        val dir = StringBuilder()
                .append(TcpDataSaver.DATA_DIR)
                .append(formatToYYMMDDHHMMSS(session.vpnStartTime))
                .append("/")
                .append(session.uniqueName)
                .toString()
        helper = TcpDataSaver(dir)
        handler = Handler(Looper.getMainLooper())
    }
}