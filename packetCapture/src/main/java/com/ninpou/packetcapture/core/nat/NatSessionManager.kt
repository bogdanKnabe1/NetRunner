package com.ninpou.packetcapture.core.nat

import com.ninpou.packetcapture.core.util.net.Packets
import java.util.concurrent.ConcurrentHashMap

object NatSessionManager {
    private const val MAX_SESSION_COUNT = 64
    private const val SESSION_TIME_OUT_NS = 60 * 1000L
    private val sessions = ConcurrentHashMap<Short, NatSession>()
    @JvmStatic
    fun getSession(portKey: Short): NatSession? {
        return sessions[portKey]
    }

    val sessionCount: Int
        get() = sessions.size

    private fun clearExpiredSessions() {
        val now = System.currentTimeMillis()
        val entries: MutableSet<MutableMap.MutableEntry<Short, NatSession>> = sessions.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (now - next.value.refreshTime > SESSION_TIME_OUT_NS) {
                iterator.remove()
            }
        }
    }

    fun clearAllSession() {
        sessions.clear()
    }

    fun getSessions(): Collection<NatSession> {
        return sessions.values
    }

    fun createSession(portKey: Short, remoteIP: Int, remotePort: Short, type: String?): NatSession {
        if (sessions.size > MAX_SESSION_COUNT) {
            clearExpiredSessions()
        }
        val session = NatSession()
        session.refreshTime = System.currentTimeMillis()
        session.remoteIP = remoteIP
        session.remotePort = remotePort
        session.localPort = portKey
        if (session.remoteHost == null) {
            session.remoteHost = Packets.ipToString(remoteIP)
        }
        session.type = type
        session.refreshIpAndPort()
        sessions[portKey] = session
        return session
    }

    fun removeSession(portKey: Short) {
        sessions.remove(portKey)
    }
}
