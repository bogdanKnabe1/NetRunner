package com.ninpou.packetcapture.core.vpn;


import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import com.ninpou.packetcapture.core.util.common.IOUtils;
import com.ninpou.packetcapture.core.util.common.Shells;

import java.io.FileDescriptor;

//LOW LEVEL VPN implementation
//set vpn address
public class VpnServiceImpl extends VpnService {
    static final int MTU = 4096;
    static final String SESSION = "NetRunner";
    static final String ADDRESS = "10.0.0.10";
    static final String ROUTE = "0.0.0.0";
    static final String DEFAULT_DNS = "114.114.114.114";

    private static final String KEY_CMD = "key_cmd";
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private long startTime;

    public VpnServiceImpl() {
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = intent.getIntExtra(KEY_CMD, 0);
        if (cmd == 0) {
            establish();
        } else {
            close();
            stopSelf();
        }
        return START_STICKY;
    }

    private void establish() {
        Builder builder = new Builder();
        builder.setMtu(MTU);
        builder.setSession(SESSION);
        builder.addAddress(ADDRESS, 0);
        builder.addRoute(ROUTE, 0);
        String dns = Shells.getDns();
        if (dns == null || dns.isEmpty()) {
            builder.addDnsServer(DEFAULT_DNS);
        } else {
            builder.addDnsServer(Shells.getDns());
        }
        vpnInterface = builder.establish();
        FileDescriptor fd = vpnInterface.getFileDescriptor();
        vpnThread = new Thread(new CoreVpnTracker(fd));
        vpnThread.start();
        VpnProxyServer.setVpnService(this);
        startTime = System.currentTimeMillis();
        VpnEventHandler.getInstance().notifyStart();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void close() {
        VpnEventHandler.getInstance().notifyStop();
        VpnProxyServer.setVpnService(null);
        if (vpnThread != null) {
            vpnThread.interrupt();
        }
        IOUtils.close(vpnInterface);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
