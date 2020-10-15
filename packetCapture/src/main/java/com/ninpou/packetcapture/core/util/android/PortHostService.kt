package com.ninpou.packetcapture.core.util.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.nat.NatSessionManager
import com.ninpou.packetcapture.core.util.net.NetFileManager
import top.srsea.lever.Lever
import kotlin.experimental.and

class PortHostService : Service() {
    private var isRefresh = false
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        NetFileManager.getInstance().init()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    val andRefreshSessionInfo: Collection<NatSession>
        get() {
            val allSession = NatSessionManager.getSessions()
            refreshSessionInfo(allSession)
            return allSession
        }

    fun refreshSessionInfo() {
        val allSession = NatSessionManager.getSessions()
        refreshSessionInfo(allSession)
    }

    private fun refreshSessionInfo(netConnections: Collection<NatSession>?) {
        if (isRefresh || netConnections == null) {
            return
        }
        var needRefresh = false
        for (connection in netConnections) {
            if (connection.appInfo == null) {
                needRefresh = true
                break
            }
        }
        if (!needRefresh) {
            return
        }
        isRefresh = true
        try {
            NetFileManager.getInstance().refresh()
            for (connection in netConnections) {
                // IF APP INFO null
                if (connection.appInfo == null) {
                    //?
                    val searchPort: Int = (connection.localPort and 0XFFFF.toShort()).toInt()
                    val uid = NetFileManager.getInstance().getUid(searchPort)
                    if (uid != null) {
                        connection.appInfo = AppInfo.createFromUid(Lever.getContext(), uid)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRefresh = false
    }

    companion object {
        @JvmStatic
        var instance: PortHostService? = null
            private set

        fun startParse(context: Context) {
            val intent = Intent(context, PortHostService::class.java)
            context.startService(intent)
        }

        fun stopParse(context: Context) {
            val intent = Intent(context, PortHostService::class.java)
            context.stopService(intent)
        }
    }
}