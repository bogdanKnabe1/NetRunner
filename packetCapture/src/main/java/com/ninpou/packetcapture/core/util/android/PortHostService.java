package com.ninpou.packetcapture.core.util.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.util.net.NetFileManager;

import java.util.Collection;

import top.srsea.lever.Lever;

public class PortHostService extends Service {
    private static PortHostService instance;
    private boolean isRefresh = false;

    public static PortHostService getInstance() {
        return instance;
    }

    public static void startParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.startService(intent);
    }

    public static void stopParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.stopService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NetFileManager.getInstance().init();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public Collection<NatSession> getAndRefreshSessionInfo() {
        Collection<NatSession> allSession = NatSessionManager.getSessions();
        refreshSessionInfo(allSession);
        return allSession;
    }

    public void refreshSessionInfo() {
        Collection<NatSession> allSession = NatSessionManager.getSessions();
        refreshSessionInfo(allSession);
    }

    private void refreshSessionInfo(Collection<NatSession> netConnections) {
        if (isRefresh || netConnections == null) {
            return;
        }
        boolean needRefresh = false;
        for (NatSession connection : netConnections) {
            if (connection.appInfo == null) {
                needRefresh = true;
                break;
            }
        }
        if (!needRefresh) {
            return;
        }
        isRefresh = true;
        try {
            NetFileManager.getInstance().refresh();

            for (NatSession connection : netConnections) {
                if (connection.appInfo == null) {
                    int searchPort = connection.localPort & 0XFFFF;
                    Integer uid = NetFileManager.getInstance().getUid(searchPort);

                    if (uid != null) {
                        connection.appInfo = AppInfo.createFromUid(Lever.getContext(), uid);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRefresh = false;
    }
}
