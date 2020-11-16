package com.b_knabe.packet_capture.core.vpn;


import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import com.b_knabe.packet_capture.core.util.common.IOUtils;
import com.b_knabe.packet_capture.core.util.common.Shells;

import java.io.FileDescriptor;

//LOW LEVEL VPN implementation
public class VpnServiceImpl extends VpnService {
    /**
     * The maximum transmission unit of the virtual network port.
     * If the length of the packet sent exceeds this number,
     * it will be sub-packaged; default set to 1500, now 4096 for more stable packaging
     */
    static final int MTU = 4096;
    static final String SESSION = "NetRunner";
    /**
     * Set the IP address of the VPN (only IPv4 is supported here)
     * This address can be checked, the address in 360 Flow Guard is 192.168.*.*;
     * Many also use 10.0.2.0 OR 10.0.0.10; not sure, you can try. Here is {@linkplain#LOCAL_IP}
     */
    static final String ADDRESS = "10.0.0.10";
    /**
     * Only the matched IP packets will be routed to the virtual port. If it is 0.0.0.0/0, all IP packets will be routed to the virtual port;
     */
    static final String ROUTE = "0.0.0.0"; // Intercept everything
    /**
     * Below are some common DNS addresses
     */
    //GOOGLE SET was CHINA
    static final String DEFAULT_DNS = "8.8.8.8";
    //unused for now
    static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    static final String AMERICA = "208.67.222.222";
    static final String CHINA_DNS_FIRST = "114.114.114.114";
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

    //need to test vpn service creation
    private void establish() {
        Builder builder = new Builder();
        builder.setMtu(MTU);
        builder.setSession(SESSION);
        builder.addAddress(ADDRESS, 0);
        builder.addRoute(ROUTE, 0);
        String dns = Shells.getDns();
        if (dns == null || dns.isEmpty()) {
            builder.addDnsServer(DEFAULT_DNS);
            // It is to add automatic completion of DNS domain name. The DNS server must be searched by the full domain name,
            // But it is too troublesome to enter the full domain name every time you look up, you can simplify it by configuring the automatic completion rule of the domain name;
            // .addSearchDomain()
            /*
             * Set the name of this session. It will be displayed in system-managed dialogs
             * and notifications. This is recommended not required.
             */
            // .setSession(getString(R.string.app_name))
        } else {
            builder.addDnsServer(Shells.getDns());
        }
        vpnInterface = builder.establish();
        FileDescriptor fd = vpnInterface.getFileDescriptor();
        vpnThread = new Thread(new CoreVpRunner(fd));
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
