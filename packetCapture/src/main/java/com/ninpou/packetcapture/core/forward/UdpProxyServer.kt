package com.ninpou.packetcapture.core.forward

import com.ninpou.packetcapture.core.tunnel.KeyHandler
import com.ninpou.packetcapture.core.tunnel.UdpTunnel
import com.ninpou.packetcapture.core.util.common.LruCache
import com.ninpou.packetcapture.core.util.common.LruCache.CleanupCallback
import com.ninpou.packetcapture.struct.Packet
import java.io.IOException
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentLinkedQueue

class UdpProxyServer(outputQueue: ConcurrentLinkedQueue<Packet>) {
    private val udpTunnels = LruCache<Short, UdpTunnel>(MAX_UDP_CACHE_SIZE,
            object : CleanupCallback<UdpTunnel> {
                override fun cleanUp(v: UdpTunnel) {
                    v.close()
                }
            })
    private val outputQueue: ConcurrentLinkedQueue<Packet>
    private var selector: Selector? = null
    private val runnable = Runnable {
        try {
            while (!Thread.interrupted()) {
                val select = selector!!.select()
                if (select == 0) {
                    Thread.sleep(5)
                }
                val keyIterator = selector!!.selectedKeys().iterator()
                while (keyIterator.hasNext()) {
                    val key = keyIterator.next()
                    if (key.isValid) {
                        try {
                            val attachment = key.attachment()
                            if (attachment is KeyHandler) {
                                attachment.onKeyReady(key)
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                    keyIterator.remove()
                }
            }
        } catch (ignored: Exception) {
        }
        stop()
    }

    fun start() {
        val thread = Thread(runnable)
        thread.start()
    }

    fun processUdpPacket(packet: Packet?, portKey: Short) {
        var udpTunnel = getUdpTunnel(portKey)
        if (udpTunnel == null) {
            udpTunnel = UdpTunnel(selector!!, this, packet!!, outputQueue, portKey)
            putUdpTunnel(portKey, udpTunnel)
            udpTunnel.initConnection()
        } else {
            udpTunnel.processPacket(packet!!)
        }
    }

    fun closeAllUdpTunnel() {
        synchronized(udpTunnels) {
            val it: MutableIterator<Map.Entry<Short, UdpTunnel>> = udpTunnels.entries.iterator()
            while (it.hasNext()) {
                it.next().value.close()
                it.remove()
            }
        }
    }

    fun closeUdpTunnel(udpTunnel: UdpTunnel) {
        synchronized(udpTunnels) {
            udpTunnel.close()
            udpTunnels.remove(udpTunnel.portKey)
        }
    }

    private fun getUdpTunnel(portKey: Short): UdpTunnel? {
        synchronized(udpTunnels) { return udpTunnels[portKey] }
    }

    private fun putUdpTunnel(ipAndPort: Short, udpTunnel: UdpTunnel) {
        synchronized(udpTunnels) { udpTunnels.put(ipAndPort, udpTunnel) }
    }

    private fun stop() {
        try {
            selector!!.close()
            selector = null
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private const val MAX_UDP_CACHE_SIZE = 50
    }

    init {
        try {
            selector = Selector.open()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        this.outputQueue = outputQueue
    }
}