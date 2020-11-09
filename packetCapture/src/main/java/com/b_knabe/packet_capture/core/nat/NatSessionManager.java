package com.b_knabe.packet_capture.core.nat;

import com.b_knabe.packet_capture.core.util.net_utils.Packets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NatSessionManager {
    /**
     * The maximum number of sessions saved
     */
    private static final int MAX_SESSION_COUNT = 64;
    /**
     * Session save time
     */
    private static final long SESSION_TIME_OUT_NS = 60 * 1000L;
    /**
     * Short is the source port of {@link NatSession#localPort}
     */
    private static final ConcurrentHashMap<Short, NatSession> sessions = new ConcurrentHashMap<>();
    /**
     * Get session information through the local port
     *
     * @param portKey local port
     * @return session information
     */
    public static NatSession getSession(short portKey) {
        return sessions.get(portKey);
    }
    /**
     * Get the number of sessions
     *
     * @return Number of sessions
     */
    public static int getSessionCount() {
        return sessions.size();
    }

    private static void clearExpiredSessions() {
        long now = System.currentTimeMillis();
        Set<Map.Entry<Short, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Short, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Short, NatSession> next = iterator.next();
            if (now - next.getValue().lastRefreshTime > SESSION_TIME_OUT_NS) {
                iterator.remove();
            }
        }
    }

    public static void clearAllSession() {
        sessions.clear();
    }

    public static Collection<NatSession> getSessions() {
        return sessions.values();
    }
    /**
     * Create a session
     *
     * @param portKey source port
     * @param remoteIP remote ip
     * @param remotePort remote port
     * @return NatSession object
     */
    public static NatSession createSession(short portKey, int remoteIP, short remotePort, String type) {
        if (sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions();
        }

        NatSession session = new NatSession();
        session.lastRefreshTime = System.currentTimeMillis();
        session.remoteIP = remoteIP;
        session.remotePort = remotePort;
        session.localPort = portKey;
        // If there is no destination ip, convert the digital ip to 192.168.0.1

        if (session.remoteHost == null) {
            session.remoteHost = Packets.ipToString(remoteIP);
        }
        session.type = type;
        session.refreshIpAndPort();
        sessions.put(portKey, session);

        return session;
    }

    public static void removeSession(short portKey) {
        sessions.remove(portKey);
    }
}