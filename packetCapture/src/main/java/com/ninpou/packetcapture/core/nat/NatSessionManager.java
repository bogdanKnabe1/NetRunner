package com.ninpou.packetcapture.core.nat;

import com.ninpou.packetcapture.core.util.net_utils.Packets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NatSessionManager {
    private static final int MAX_SESSION_COUNT = 64;
    private static final long SESSION_TIME_OUT_NS = 60 * 1000L;
    private static final ConcurrentHashMap<Short, NatSession> sessions = new ConcurrentHashMap<>();

    public static NatSession getSession(short portKey) {
        return sessions.get(portKey);
    }

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

    public static NatSession createSession(short portKey, int remoteIP, short remotePort, String type) {
        if (sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions();
        }

        NatSession session = new NatSession();
        session.lastRefreshTime = System.currentTimeMillis();
        session.remoteIP = remoteIP;
        session.remotePort = remotePort;
        session.localPort = portKey;


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