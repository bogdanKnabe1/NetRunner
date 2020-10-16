package com.ninpou.packetcapture.core.util.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.ninpou.packetcapture.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AppInfo implements Serializable {
    private static Drawable defaultIcon = null;
    private static final LruCache<String, IconInfo> iconCache = new LruCache<>(50);
    public final String allAppName;
    public final String leaderAppName;
    public final PackageNames pkgs;

    static class Entry {
        final String appName;
        final String pkgName;

        public Entry(String appName, String pkgName) {
            this.appName = appName;
            this.pkgName = pkgName;
        }
    }

    static class IconInfo {
        long date;
        Drawable icon;

        IconInfo() {
        }
    }

    private AppInfo(String leaderAppName, String allAppName, String[] pkgs) {
        this.leaderAppName = leaderAppName;
        this.allAppName = allAppName;
        this.pkgs = PackageNames.newInstance(pkgs);
    }

    public static AppInfo createFromUid(Context ctx, int uid) {
        PackageManager pm = ctx.getPackageManager();
        ArrayList<Entry> list = new ArrayList<>();
        if (uid > 0) {
            try {
                String[] pkgNames = pm.getPackagesForUid(uid);
                if (pkgNames == null || pkgNames.length <= 0) {
                    list.add(new Entry("System", "nonpkg.noname"));
                } else {
                    for (String pkgName : pkgNames) {
                        if (!TextUtils.isEmpty(pkgName)) {
                            try {
                                PackageInfo appPackageInfo = pm.getPackageInfo(pkgName, 0);
                                String appName = null;
                                if (appPackageInfo != null) {
                                    appName = appPackageInfo.applicationInfo.loadLabel(pm).toString();
                                }
                                if (TextUtils.isEmpty(appName)) {
                                    // If the app name is empty, use the package name instead
                                    appName = pkgName;
                                }
                                list.add(new Entry(appName, pkgName));
                            } catch (PackageManager.NameNotFoundException e) {
                                // When the app corresponding to the package name is not found on the mobile phone system
                                list.add(new Entry(pkgName, pkgName));
                            }
                        }
                    }
                }
            } catch (RuntimeException e2) {
                Log.i("RuntimeException", "error getPackagesForUid(). package manager has died");
                return null;
            }
        }
        if (list.size() == 0) {
            // Only when uid <= 0, list.size() is 0, and the default is system
            list.add(new Entry("System", "root.uid=0"));
        }
        Collections.sort(list, new Comparator<Entry>() {
            @Override
            public int compare(Entry lhs, Entry rhs) {
                int ret = lhs.appName.compareToIgnoreCase(rhs.appName);
                if (ret == 0) {
                    return lhs.pkgName.compareToIgnoreCase(rhs.pkgName);
                }
                return ret;
            }
        });
        String[] pkgs = new String[list.size()];
        String[] apps = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            pkgs[i] = list.get(i).pkgName;
            apps[i] = list.get(i).appName;
        }
        return new AppInfo(apps[0], TextUtils.join(",", apps), pkgs);
    }

    public static Drawable getIcon(Context ctx, String pkgName) {
        return getIcon(ctx, pkgName, false);
    }

    public static synchronized Drawable getIcon(Context ctx, String pkgName, boolean onlyPeek) {
        Drawable drawable = null;
        synchronized (AppInfo.class) {
            IconInfo iconInfo;
            if (defaultIcon == null) {
                defaultIcon = ctx.getResources().getDrawable(R.drawable.sym_def_app_icon);
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo appPackageInfo = null;
            try {
                appPackageInfo = pm.getPackageInfo(pkgName, 0);
                long lastUpdate = appPackageInfo.lastUpdateTime;
                iconInfo = (IconInfo) iconCache.get(pkgName);
                if (iconInfo != null && iconInfo.date == lastUpdate && iconInfo.icon != null) {
                    drawable = iconInfo.icon;
                }
            } catch (PackageManager.NameNotFoundException e) {
                //
            }
            if (appPackageInfo != null) {
                if (!onlyPeek) {
                    drawable = appPackageInfo.applicationInfo.loadIcon(pm);
                    iconInfo = new IconInfo();
                    iconInfo.date = appPackageInfo.lastUpdateTime;
                    iconInfo.icon = drawable;
                    iconCache.put(pkgName, iconInfo);
                }
            } else {
                iconCache.remove(pkgName);
                drawable = defaultIcon;
            }
        }
        return drawable;
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder() ;
        stringBuffer.append("{\"allAppName\":").append("\"").append(allAppName).append("\",")
                .append("\"leaderAppName\":").append("\"").append(leaderAppName).append("\"");
        if(pkgs != null && pkgs.pkgs.length > 0){
            stringBuffer.append(",") ;
            for(int i = 0 ; i < pkgs.pkgs.length ; i++){
                stringBuffer.append("\"").append(i).append("\":").append("\"").append(pkgs.pkgs[i]).append("\"") ;
                if(i == pkgs.pkgs.length - 1){
                    stringBuffer.append("}") ;
                }else {
                    stringBuffer.append(",") ;
                }
            }
        }else {
            stringBuffer.append("}") ;
        }
        return stringBuffer.toString() ;
    }
}

