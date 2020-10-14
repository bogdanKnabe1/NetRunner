package com.ninpou.packetcapture.core.tunnel

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ninpou.packetcapture.core.forward.UdpProxyServer
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.nat.NatSessionManager
import com.ninpou.packetcapture.core.util.android.PortHostService
import com.ninpou.packetcapture.core.util.common.ACache
import com.ninpou.packetcapture.core.util.common.ThreadPool
import com.ninpou.packetcapture.core.util.common.TimeFormatter.formatToYYMMDDHHMMSS
import com.ninpou.packetcapture.core.util.net.TcpDataSaver
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.mtu
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.protect
import com.ninpou.packetcapture.struct.Packet
import top.srsea.torque.common.IOUtils
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class UdpTunnel(private val selector: Selector, private val udpProxyServer: UdpProxyServer, val referencePacket: Packet, outputQueue: Queue<Packet>, portKey: Short) : KeyHandler {
    private val outputQueue: Queue<Packet>
    private val toNetWorkPackets = ConcurrentLinkedQueue<Packet>()
    private val session: NatSession
    private val handler: Handler
    private val ipAndPort: String
    private val tcpDataSaver: TcpDataSaver?
    private var selectionKey: SelectionKey? = null
    var channel: DatagramChannel? = null
        private set
    val portKey: Short
    private fun processKey(key: SelectionKey?) {
        if (key!!.isWritable) {
            processSend()
        } else if (key.isReadable) {
            processReceived()
        }
        updateInterests()
    }

    private fun processReceived() {
        val receiveBuffer = ByteBuffer.allocate(mtu)
        receiveBuffer.position(HEADER_SIZE)
        val readBytes: Int
        readBytes = try {
            channel!!.read(receiveBuffer)
        } catch (e: Exception) {
            udpProxyServer.closeUdpTunnel(this)
            return
        }
        if (readBytes == -1) {
            udpProxyServer.closeUdpTunnel(this)
        } else if (readBytes == 0) {
            Log.d(TAG, "read no data :$ipAndPort")
        } else {
            Log.d(TAG, "read readBytes:$readBytes ipAndPort:$ipAndPort")
            val newPacket = referencePacket.duplicated()
            newPacket.updateUDPBuffer(receiveBuffer, readBytes)
            receiveBuffer.position(HEADER_SIZE + readBytes)
            outputQueue.offer(newPacket)
            session.receivePacketNum++
            session.receiveByteNum += readBytes.toLong()
            session.lastRefreshTime = System.currentTimeMillis()
            if (tcpDataSaver != null) {
                saveData(receiveBuffer.array(), readBytes, false)
            }
        }
    }

    private fun saveData(array: ByteArray, saveSize: Int, isRequest: Boolean) {
        val saveTcpData = TcpDataSaver.TcpData.Builder()
                .offSet(HEADER_SIZE)
                .length(saveSize)
                .needParseData(array)
                .isRequest(isRequest)
                .build()
        tcpDataSaver!!.addData(saveTcpData)
    }

    private fun processSend() {
        Log.d(TAG, "processWriteUDPData $ipAndPort")
        val toNetWorkPacket = getToNetWorkPackets()
        if (toNetWorkPacket == null) {
            Log.d(TAG, "write data  no packet ")
            return
        }
        try {
            val payloadBuffer = toNetWorkPacket.backingBuffer
            session.packetSent++
            val sendSize = payloadBuffer.limit() - payloadBuffer.position()
            session.bytesSent += sendSize
            if (tcpDataSaver != null) {
                saveData(payloadBuffer.array(), sendSize, true)
            }
            session.lastRefreshTime = System.currentTimeMillis()
            while (payloadBuffer.hasRemaining()) {
                channel!!.write(payloadBuffer)
            }
        } catch (e: IOException) {
            Log.w(TAG, "Network write error: $ipAndPort", e)
            udpProxyServer.closeUdpTunnel(this)
        }
    }

    fun initConnection() {
        Log.d(TAG, "init  ipAndPort:$ipAndPort")
        val destinationAddress = referencePacket.ip4Header.destinationAddress
        val destinationPort = referencePacket.udpHeader.destinationPort
        try {
            //? NULL
            channel = DatagramChannel.open()
            protect(channel?.socket())
            channel?.configureBlocking(false)
            channel?.connect(InetSocketAddress(destinationAddress, destinationPort))
            selector.wakeup()
            selectionKey = channel!!.register(selector,
                    SelectionKey.OP_READ, this)
        } catch (e: IOException) {
            IOUtils.close(channel)
            return
        }
        referencePacket.swapSourceAndDestination()
        addToNetWorkPacket(referencePacket)
    }

    fun processPacket(packet: Packet) {
        addToNetWorkPacket(packet)
        updateInterests()
    }

    fun close() {
        try {
            if (selectionKey != null) {
                selectionKey!!.cancel()
            }
            if (channel != null) {
                channel!!.close()
            }
            if (session.appInfo == null && PortHostService.instance != null) {
                PortHostService.instance!!.refreshSessionInfo()
            }
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
        } catch (e: Exception) {
            Log.w(TAG, "error to close UDP channel IpAndPort" + ipAndPort + ",error is " + e.message)
        }
    }

    fun getToNetWorkPackets(): Packet? {
        return toNetWorkPackets.poll()
    }

    private fun addToNetWorkPacket(packet: Packet) {
        toNetWorkPackets.offer(packet)
        updateInterests()
    }

    private fun updateInterests() {
        val ops: Int
        ops = if (toNetWorkPackets.isEmpty()) {
            SelectionKey.OP_READ
        } else {
            SelectionKey.OP_WRITE or SelectionKey.OP_READ
        }
        selector.wakeup()
        selectionKey!!.interestOps(ops)
    }

    override fun onKeyReady(key: SelectionKey?) {
        processKey(key)
    }

    companion object {
        private const val TAG = "UdpTunnel"
        private const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE
    }

    init {
        ipAndPort = referencePacket.ipAndPort
        this.outputQueue = outputQueue
        this.portKey = portKey
        session = NatSessionManager.getSession(portKey)
        handler = Handler(Looper.getMainLooper())
        val dir = StringBuilder()
                .append(TcpDataSaver.DATA_DIR)
                .append(formatToYYMMDDHHMMSS(session.vpnStartTime))
                .append("/")
                .append(session.uniqueName)
                .toString()
        tcpDataSaver = TcpDataSaver(dir)
    }
}