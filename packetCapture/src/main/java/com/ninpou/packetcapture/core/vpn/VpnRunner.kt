package com.ninpou.packetcapture.core.vpn

import com.ninpou.packetcapture.core.forward.TcpProxyServer
import com.ninpou.packetcapture.core.forward.UdpProxyServer
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.nat.NatSessionManager
import com.ninpou.packetcapture.core.util.android.PortHostService
import com.ninpou.packetcapture.core.util.common.ThreadPool
import com.ninpou.packetcapture.core.util.net.HttpRequestHeaderParser
import com.ninpou.packetcapture.core.util.net.Packets
import com.ninpou.packetcapture.core.vpn.VpnEvent.Companion.instance
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.address
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.startTime
import com.ninpou.packetcapture.struct.IpHeader
import com.ninpou.packetcapture.struct.Packet
import com.ninpou.packetcapture.struct.TcpHeader
import com.ninpou.packetcapture.struct.UdpHeader
import top.srsea.lever.Lever
import top.srsea.torque.common.IOUtils
import java.io.*
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class VpnRunner internal constructor(fd: FileDescriptor?) : Runnable {
    private var tcpProxyServer: TcpProxyServer? = null
    private val udpProxyServer: UdpProxyServer
    private val out: OutputStream
    private val `in`: InputStream
    private val udpQueue: ConcurrentLinkedQueue<Packet>

    @Throws(IOException::class)
    private fun handlePacket(data: ByteArray, len: Int): Boolean {
        val ipHeader = IpHeader(data, 0)
        return when (ipHeader.protocol) {
            IpHeader.TCP -> handleTcpPacket(ipHeader, len)
            IpHeader.UDP -> handleUdpPacket(ipHeader, len)
            else -> false
        }
    }

    @Throws(IOException::class)
    private fun handleTcpPacket(ipHeader: IpHeader, len: Int): Boolean {
        val tcpHeader = TcpHeader(ipHeader.data, ipHeader.headerLength)
        val srcPort = tcpHeader.sourcePort
        if (srcPort == tcpProxyServer!!.port) {
            val session = NatSessionManager.getSession(tcpHeader.destinationPort)
            if (session != null) {
                ipHeader.sourceIP = ipHeader.destinationIP
                tcpHeader.sourcePort = session.remotePort
                ipHeader.destinationIP = Packets.ipToInt(address)
                Packets.computeTcpChecksum(ipHeader, tcpHeader)
                out.write(ipHeader.data, ipHeader.offset, len)
            }
        } else {
            var session = NatSessionManager.getSession(srcPort)
            if (session == null || session.remoteIP != ipHeader.destinationIP || session.remotePort != tcpHeader.destinationPort) {
                session = NatSessionManager.createSession(srcPort, ipHeader.destinationIP, tcpHeader
                        .destinationPort, NatSession.TCP)
                session.vpnStartTime = startTime
                ThreadPool.instance.execute {
                    val instance = PortHostService.instance
                    instance?.refreshSessionInfo()
                }
            }
            session!!.lastRefreshTime = System.currentTimeMillis()
            session.packetSent++
            val tcpDataSize = ipHeader.dataLength - tcpHeader.headerLength
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false
            }
            if (session.bytesSent == 0 && tcpDataSize > 10) {
                val dataOffset = tcpHeader.offset + tcpHeader.headerLength
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.data, dataOffset,
                        tcpDataSize)
            } else if (session.bytesSent > 0 && !session.isHttpsSession
                    && session.isHttp
                    && session.remoteHost == null && session.requestUrl == null) {
                val dataOffset = tcpHeader.offset + tcpHeader.headerLength
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpHeader.data, dataOffset,
                        tcpDataSize)
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl
            }
            ipHeader.sourceIP = ipHeader.destinationIP
            ipHeader.destinationIP = Packets.ipToInt(address)
            tcpHeader.destinationPort = tcpProxyServer!!.port
            Packets.computeTcpChecksum(ipHeader, tcpHeader)
            out.write(ipHeader.data, ipHeader.offset, len)
            session.bytesSent += tcpDataSize
        }
        instance.notifyReceive()
        return true
    }

    @Throws(UnknownHostException::class)
    private fun handleUdpPacket(ipHeader: IpHeader, len: Int): Boolean {
        val udpHeader = UdpHeader(ipHeader.data, ipHeader.headerLength)
        val srcPort = udpHeader.sourcePort
        var session = NatSessionManager.getSession(srcPort)
        if (session == null || session.remoteIP != ipHeader.destinationIP || (session.remotePort
                        != udpHeader.destinationPort)) {
            session = NatSessionManager.createSession(srcPort, ipHeader.destinationIP, udpHeader
                    .destinationPort, NatSession.UDP)
            session.vpnStartTime = startTime
            ThreadPool.instance.execute {
                if (PortHostService.instance != null) {
                    PortHostService.instance!!.refreshSessionInfo()
                }
            }
        }
        session!!.lastRefreshTime = System.currentTimeMillis()
        session.packetSent++
        val bytes = Arrays.copyOf(ipHeader.data, ipHeader.data.size)
        val byteBuffer = ByteBuffer.wrap(bytes, 0, len)
        byteBuffer.limit(len)
        val packet = Packet(byteBuffer)
        udpProxyServer.processUdpPacket(packet, srcPort)
        return false
    }

    override fun run() {
        val buf = ByteArray(VpnServiceImpl.MTU)
        try {
            while (!Thread.interrupted()) {
                val len = `in`.read(buf)
                if (len == 0) {
                    Thread.sleep(10)
                    continue
                } else if (len == -1) {
                    break
                }
                if (!handlePacket(buf, len)) {
                    val packet = udpQueue.poll() ?: continue
                    val buffer = packet.backingBuffer
                    out.write(buffer.array())
                }
            }
        } catch (ignored: IOException) {
        } catch (ignored: InterruptedException) {
        } finally {
            IOUtils.close(`in`, out)
            tcpProxyServer!!.stop()
            udpProxyServer.closeAllUdpTunnel()
            NatSessionManager.clearAllSession()
            PortHostService.stopParse(Lever.getContext())
        }
    }

    init {
        `in` = FileInputStream(fd)
        out = FileOutputStream(fd)
        udpQueue = ConcurrentLinkedQueue()
        try {
            tcpProxyServer = TcpProxyServer(0)
            tcpProxyServer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        udpProxyServer = UdpProxyServer(udpQueue)
        udpProxyServer.start()
        PortHostService.startParse(Lever.getContext())
    }
}