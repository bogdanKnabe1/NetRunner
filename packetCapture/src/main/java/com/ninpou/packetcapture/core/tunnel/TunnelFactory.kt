package com.ninpou.packetcapture.core.tunnel

import com.ninpou.packetcapture.core.nat.NatSessionManager
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

object TunnelFactory {
    @JvmStatic
    fun wrap(channel: SocketChannel, selector: Selector?): BaseTcpTunnel {
        val tunnel: BaseTcpTunnel = RawTcpTunnel(channel, selector)
        val session = NatSessionManager.getSession(channel.socket().port.toShort())
        if (session != null) {
            tunnel.isHttpsRequest(session.isHttpsSession)
        }
        return tunnel
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createTunnelByConfig(destAddress: InetSocketAddress?, selector: Selector?, portKey: Short): BaseTcpTunnel {
        return RemoteTcpTunnel(destAddress, selector, portKey)
    }
}