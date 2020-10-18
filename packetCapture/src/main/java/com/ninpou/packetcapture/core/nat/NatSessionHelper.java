package com.ninpou.packetcapture.core.nat;

import com.ninpou.packetcapture.core.util.common.FileManager;
import com.ninpou.packetcapture.core.util.common.ACache;
import com.ninpou.packetcapture.core.util.common.TimeFormatter;
import com.ninpou.packetcapture.core.util.net.TcpDataSaver;
import com.ninpou.packetcapture.core.util.processparse.PortHostService;
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy;
import java.io.File;
import java.util.*;

public class NatSessionHelper {
    public static Collection<NatSession> getAllSessions() {
        File file = new File(TcpDataSaver.CONFIG_DIR
                + TimeFormatter.formatToYYMMDDHHMMSS(VpnServiceProxy.getStartTime()));
        ACache aCache = ACache.get(file);
        String[] list = file.list();
        ArrayList<NatSession> baseNetSessions = new ArrayList<>();
        if (list != null) {
            for (String fileName : list) {
                NatSession netConnection = (NatSession) aCache.getAsObject(fileName);
                baseNetSessions.add(netConnection);
            }
        }
        PortHostService portHostService = PortHostService.getInstance();
        if (portHostService != null) {
            Collection<NatSession> aliveConnInfo = portHostService.getAndRefreshSessionInfo();
            if (aliveConnInfo != null) {
                baseNetSessions.addAll(aliveConnInfo);
            }
        }
        Collections.sort(baseNetSessions, new Comparator<NatSession>() {
            @Override
            public int compare(NatSession o1, NatSession o2) {
                return Long.compare(o2.lastRefreshTime, o1.lastRefreshTime);
            }
        });
        return baseNetSessions;
    }

    public static void clearCache() {
        String data = TcpDataSaver.DATA_DIR;
        String config = TcpDataSaver.CONFIG_DIR;
        File dataDir = new File(data);
        File configDir = new File(config);
        FileManager.deleteUnder(dataDir);
        FileManager.deleteUnder(configDir);
        NatSessionManager.clearAllSession();
    }
}
