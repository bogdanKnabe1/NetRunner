package com.ninpou.packetcapture.core.vpn

import java.lang.ref.WeakReference
import java.net.DatagramSocket
import java.net.Socket

object VpnServiceProxy {
    private var vpnService: WeakReference<VpnServiceImpl?>? = null
    @JvmStatic
    fun setVpnService(vpnService: VpnServiceImpl?) {
        VpnServiceProxy.vpnService = WeakReference(vpnService)
    }

    @JvmStatic
    fun protect(socket: Socket?): Boolean {
        return if (vpnService == null || vpnService!!.get() == null) false else vpnService!!.get()!!.protect(socket)
    }

    @JvmStatic
    fun protect(socket: DatagramSocket?): Boolean {
        return if (vpnService == null || vpnService!!.get() == null) false else vpnService!!.get()!!.protect(socket)
    }

    @JvmStatic
    val mtu: Int
        get() = VpnServiceImpl.MTU
    @JvmStatic
    val address: String
        get() = VpnServiceImpl.ADDRESS
    @JvmStatic
    val startTime: Long
        get() = if (vpnService == null || vpnService!!.get() == null) -1 else vpnService!!.get()!!.startTime
}