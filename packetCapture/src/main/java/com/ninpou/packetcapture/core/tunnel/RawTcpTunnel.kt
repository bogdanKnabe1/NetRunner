package com.ninpou.packetcapture.core.tunnel

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

open class RawTcpTunnel : BaseTcpTunnel {
    constructor(innerChannel: SocketChannel?, selector: Selector?) : super(innerChannel, selector) {}
    constructor(serverAddress: InetSocketAddress?, selector: Selector?, portKey: Short) : super(serverAddress, selector, portKey) {}

    @Throws(Exception::class)
    override fun onConnected() {
        onTunnelEstablished()
    }

    override val isTunnelEstablished: Boolean
        protected get() = true

    @Throws(Exception::class)
    override fun beforeSend(buffer: ByteBuffer?) {
    }

    @Throws(Exception::class)
    override fun afterReceived(buffer: ByteBuffer?) {
    }

    override fun onDispose() {}
}