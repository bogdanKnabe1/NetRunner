package com.ninpou.packetcapture.core.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.ninpou.packetcapture.core.util.common.Shells
import com.ninpou.packetcapture.core.vpn.VpnEvent.Companion.instance
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy.setVpnService
import top.srsea.torque.common.IOUtils

class VpnServiceImpl : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null
    var startTime: Long = 0
        private set

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cmd = intent.getIntExtra(KEY_CMD, 0)
        if (cmd == 0) {
            establish()
        } else {
            close()
            stopSelf()
        }
        return START_STICKY
    }

    private fun establish() {
        val builder = Builder()
        builder.setMtu(MTU)
        builder.setSession(SESSION)
        builder.addAddress(ADDRESS, 0)
        builder.addRoute(ROUTE, 0)
        val dns = Shells.getDns()
        if (dns == null || dns.isEmpty()) {
            builder.addDnsServer(DEFAULT_DNS)
        } else {
            builder.addDnsServer(Shells.getDns())
        }
        vpnInterface = builder.establish()
        val fd = vpnInterface!!.fileDescriptor
        vpnThread = Thread(VpnRunner(fd))
        vpnThread!!.start()
        setVpnService(this)
        startTime = System.currentTimeMillis()
        instance.notifyStart()
    }

    override fun onCreate() {
        super.onCreate()
    }

    private fun close() {
        instance.notifyStop()
        setVpnService(null)
        if (vpnThread != null) {
            vpnThread!!.interrupt()
        }
        IOUtils.close(vpnInterface)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val MTU = 4096
        const val SESSION = "Stream"
        const val ADDRESS = "10.0.0.10"
        const val ROUTE = "0.0.0.0"
        const val DEFAULT_DNS = "114.114.114.114"
        private const val KEY_CMD = "key_cmd"
    }
}