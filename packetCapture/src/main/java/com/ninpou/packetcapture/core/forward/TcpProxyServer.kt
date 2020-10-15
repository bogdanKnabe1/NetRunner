package com.ninpou.packetcapture.core.forward

import android.util.Log
import com.ninpou.packetcapture.core.nat.NatSessionManager.getSession
import com.ninpou.packetcapture.core.tunnel.BaseTcpTunnel
import com.ninpou.packetcapture.core.tunnel.KeyHandler
import com.ninpou.packetcapture.core.tunnel.TunnelFactory.createTunnelByConfig
import com.ninpou.packetcapture.core.tunnel.TunnelFactory.wrap
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.experimental.and

class TcpProxyServer(port: Int) {
    private var selector: Selector? = null
    private var serverSocketChannel: ServerSocketChannel?
    private var serverThread: Thread? = null
    var isStopped = false
        private set
    val port: Short

    private val runnable = Runnable {
        try {
            while (!Thread.interrupted()) {
                val select = selector?.select()
                if (select == 0) {
                    Thread.sleep(5)
                    continue
                }
                val selectionKeys = selector?.selectedKeys() ?: continue
                val keyIterator = selector!!.selectedKeys().iterator()
                while (keyIterator.hasNext()) {
                    val key = keyIterator.next()
                    if (key.isValid) {
                        try {
                            if (key.isAcceptable) {
                                Log.d(TAG, "isAcceptable")
                                onAccepted(key)
                            } else {
                                val attachment = key.attachment()
                                if (attachment is KeyHandler) {
                                    attachment.onKeyReady(key)
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                    keyIterator.remove()
                }
            }
        } catch (ignored: Exception) {
        } finally {
            stop()
        }
    }

    fun start() {
        serverThread = Thread(runnable)
        serverThread!!.start()
    }

    fun stop() {
        isStopped = true
        if (selector != null) {
            try {
                selector!!.close()
                selector = null
            } catch (ignored: Exception) {
            }
        }
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel!!.close()
                serverSocketChannel = null
            } catch (ignored: Exception) {
            }
        }
    }

    private fun getDestAddress(localChannel: SocketChannel): InetSocketAddress? {
        val portKey = localChannel.socket().port.toShort()
        val session = getSession(portKey)
        return if (session != null) {
            InetSocketAddress(localChannel.socket().inetAddress, (session.remotePort and 0xFFFF.toShort()).toInt())
        } else null
    }

    private fun onAccepted(key: SelectionKey) {
        var localTunnel: BaseTcpTunnel? = null
        try {
            val localChannel = serverSocketChannel!!.accept()
            localTunnel = wrap(localChannel, selector)
            val portKey = localChannel.socket().port.toShort()
            val destAddress = getDestAddress(localChannel)
            if (destAddress != null) {
                val remoteTunnel = createTunnelByConfig(destAddress, selector, portKey)
                remoteTunnel.setIsHttpsRequest(localTunnel.isHttpsRequest)
                remoteTunnel.setBrotherTunnel(localTunnel)
                localTunnel.setBrotherTunnel(remoteTunnel)
                remoteTunnel.connect(destAddress)
            }
        } catch (ex: Exception) {
            localTunnel?.dispose()
        }
    }

    companion object {
        private const val TAG = "TcpProxyServer"
    }

    init {
        selector = Selector.open()
        serverSocketChannel = ServerSocketChannel.open()
        serverSocketChannel?.configureBlocking(false)
        serverSocketChannel?.socket()?.bind(InetSocketAddress(port))
        serverSocketChannel?.register(selector, SelectionKey.OP_ACCEPT)
        // NULL CARE!!
        this.port = serverSocketChannel!!.socket().localPort.toShort()
    }
}