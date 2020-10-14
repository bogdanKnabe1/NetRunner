package com.ninpou.packetcapture.core.tunnel

import com.ninpou.packetcapture.core.nat.NatSessionManager
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.mtu
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.protect
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentLinkedQueue

abstract class BaseTcpTunnel : KeyHandler {
    protected var destAddress: InetSocketAddress? = null
    var needWriteData = ConcurrentLinkedQueue<ByteBuffer>()
    private var portKey: Short = 0
    private var innerChannel: SocketChannel?
    private var selector: Selector?
    var isHttpsRequest = false
    private var brotherTunnel: BaseTcpTunnel? = null
    private var disposed = false
    private var serverEP: InetSocketAddress? = null

    constructor(innerChannel: SocketChannel?, selector: Selector?) {
        this.innerChannel = innerChannel
        this.selector = selector
        sessionCount++
    }

    constructor(serverAddress: InetSocketAddress?, selector: Selector?, portKey: Short) {
        val innerChannel = SocketChannel.open()
        innerChannel.configureBlocking(false)
        this.innerChannel = innerChannel
        this.selector = selector
        serverEP = serverAddress
        this.portKey = portKey
        sessionCount++
    }

    override fun onKeyReady(key: SelectionKey?) {
        if (key!!.isReadable) {
            onReadable(key)
        } else if (key.isWritable) {
            onWritable(key)
        } else if (key.isConnectable) {
            onConnectable()
        }
    }

    @Throws(Exception::class)
    protected abstract fun onConnected()
    protected abstract val isTunnelEstablished: Boolean
    @Throws(Exception::class)
    protected abstract fun beforeSend(buffer: ByteBuffer?)
    @Throws(Exception::class)
    protected abstract fun afterReceived(buffer: ByteBuffer?)
    protected abstract fun onDispose()
    fun setBrotherTunnel(brotherTunnel: BaseTcpTunnel?) {
        this.brotherTunnel = brotherTunnel
    }

    @Throws(Exception::class)
    fun connect(destAddress: InetSocketAddress?) {
        if (protect(innerChannel!!.socket())) {
            this.destAddress = destAddress
            innerChannel!!.register(selector, SelectionKey.OP_CONNECT, this)
            innerChannel!!.connect(serverEP)
        } else {
            throw Exception("VPN protect socket failed.")
        }
    }

    fun onConnectable() {
        try {
            if (innerChannel!!.finishConnect()) {
                onConnected()
            } else {
                dispose()
            }
        } catch (e: Exception) {
            dispose()
        }
    }

    @Throws(Exception::class)
    protected fun beginReceived() {
        if (innerChannel!!.isBlocking) {
            innerChannel!!.configureBlocking(false)
        }
        selector!!.wakeup()
        innerChannel!!.register(selector, SelectionKey.OP_READ, this)
    }

    fun onReadable(key: SelectionKey?) {
        try {
            val buffer = ByteBuffer.allocate(mtu)
            buffer.clear()
            val bytesRead = innerChannel!!.read(buffer)
            if (bytesRead > 0) {
                buffer.flip()
                afterReceived(buffer)
                sendToBrother(key, buffer)
            } else if (bytesRead < 0) {
                dispose()
            }
        } catch (ex: Exception) {
            dispose()
        }
    }

    @Throws(Exception::class)
    protected fun sendToBrother(key: SelectionKey?, buffer: ByteBuffer) {
        if (isTunnelEstablished && buffer.hasRemaining()) {
            brotherTunnel!!.getWriteDataFromBrother(buffer)
        }
    }

    private fun getWriteDataFromBrother(buffer: ByteBuffer) {
        if (buffer.hasRemaining() && needWriteData.size == 0) {
            var writeSize: Int
            try {
                writeSize = write(buffer)
            } catch (e: Exception) {
                writeSize = 0
                e.printStackTrace()
            }
            if (writeSize > 0) {
                return
            }
        }
        needWriteData.offer(buffer)
        try {
            selector!!.wakeup()
            innerChannel!!.register(selector, SelectionKey.OP_READ or SelectionKey.OP_WRITE, this)
        } catch (e: ClosedChannelException) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    protected fun write(buffer: ByteBuffer): Int {
        var byteSendSum = 0
        beforeSend(buffer)
        while (buffer.hasRemaining()) {
            val byteSent = innerChannel!!.write(buffer)
            byteSendSum += byteSent
            if (byteSent == 0) {
                break
            }
        }
        return byteSendSum
    }

    fun onWritable(key: SelectionKey?) {
        try {
            val mSendRemainBuffer = needWriteData.poll() ?: return
            write(mSendRemainBuffer)
            if (needWriteData.size == 0) {
                try {
                    selector!!.wakeup()
                    innerChannel!!.register(selector, SelectionKey.OP_READ, this)
                } catch (e: ClosedChannelException) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            dispose()
        }
    }

    @Throws(Exception::class)
    protected fun onTunnelEstablished() {
        beginReceived()
        brotherTunnel!!.beginReceived()
    }

    fun dispose() {
        disposeInternal(true)
    }

    fun disposeInternal(disposeBrother: Boolean) {
        if (!disposed) {
            try {
                innerChannel!!.close()
            } catch (ignored: Exception) {
            }
            if (brotherTunnel != null && disposeBrother) {
                brotherTunnel!!.disposeInternal(false)
            }
            innerChannel = null
            selector = null
            brotherTunnel = null
            disposed = true
            --sessionCount
            onDispose()
            NatSessionManager.removeSession(portKey)
        }
    }

    open fun setIsHttpsRequest(isHttpsRequest: Boolean) {
        this.isHttpsRequest = isHttpsRequest
    }


    open fun isHttpsRequest(httpsSession: Boolean): Boolean {
        return isHttpsRequest
    }

    companion object {
        var sessionCount: Long = 0
    }
}