package com.ninpou.packetcapture.core.util.process_parse;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ninpou.packetcapture.Qbits;
import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;

import java.util.Collection;

public class PortSessionInfoService extends Service {
    private static PortSessionInfoService instance;
    private boolean isRefresh = false;

    public static PortSessionInfoService getInstance() {
        return instance;
    }

    public static void startParse(Context context) {
        Intent intent = new Intent(context, PortSessionInfoService.class);
        context.startService(intent);
    }

    public static void stopParse(Context context) {
        Intent intent = new Intent(context, PortSessionInfoService.class);
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
        NetWorkFileManager.getInstance().init();
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
            if (connection.applicationInfo == null) {
                needRefresh = true;
                break;
            }
        }
        if (!needRefresh) {
            return;
        }
        isRefresh = true;
        try {
            NetWorkFileManager.getInstance().refresh();

            for (NatSession connection : netConnections) {
                if (connection.applicationInfo == null) {
                    int searchPort = connection.localPort & 0XFFFF;
                    Integer uid = NetWorkFileManager.getInstance().getUid(searchPort);

                    if (uid != null) {
                        connection.applicationInfo = ApplicationInfo.createFromUid(Qbits.getAppContext(), uid);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRefresh = false;
    }
}
