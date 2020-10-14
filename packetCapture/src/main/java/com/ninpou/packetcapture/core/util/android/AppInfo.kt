package com.ninpou.packetcapture.core.util.android

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.ninpou.packetcapture.R
import com.ninpou.packetcapture.core.util.android.PackageNames.Companion.newInstance
import java.io.Serializable
import java.util.*

class AppInfo private constructor(val leaderAppName: String?, val allAppName: String, packageNames: Array<String?>) : Serializable {

    val packageNames: PackageNames = newInstance(packageNames)

    internal class Entry(val appName: String, val pkgName: String)
    internal class IconInfo {
        var date: Long = 0
        var icon: Drawable? = null
    }

    companion object {
        private const val TAG = "AppInfo"
        private val iconCache = LruCache<String, IconInfo>(50)
        private var defaultIcon: Drawable? = null
        fun createFromUid(ctx: Context, uid: Int): AppInfo? {
            val pm = ctx.packageManager
            val list = ArrayList<Entry>()
            if (uid > 0) {
                try {
                    val pkgNames = pm.getPackagesForUid(uid)
                    if (pkgNames == null || pkgNames.isEmpty()) {
                        list.add(Entry("System", "nonpkg.noname"))
                    } else {
                        for (pkgName in pkgNames) {
                            if (pkgName != null) {
                                try {
                                    val appPackageInfo = pm.getPackageInfo(pkgName, 0)
                                    var appName: String? = null
                                    if (appPackageInfo != null) {
                                        appName = appPackageInfo.applicationInfo.loadLabel(pm).toString()
                                    }
                                    if (appName == null || appName == "") {
                                        appName = pkgName
                                    }
                                    list.add(Entry(appName, pkgName))
                                } catch (e: PackageManager.NameNotFoundException) {
                                    list.add(Entry(pkgName, pkgName))
                                }
                            }
                        }
                    }
                } catch (e: RuntimeException) {
                    Log.i(TAG, "error getPackagesForUid(). package manager has died")
                    return null
                }
            }
            if (list.size == 0) {
                list.add(Entry("System", "root.uid=0"))
            }
            list.sortWith { lhs, rhs ->
                val ret = lhs.appName.compareTo(rhs.appName, ignoreCase = true)
                if (ret == 0) {
                    lhs.pkgName.compareTo(rhs.pkgName, ignoreCase = true)
                } else ret
            }
            val pkgs = arrayOfNulls<String>(list.size)
            val apps = arrayOfNulls<String>(list.size)
            for (i in list.indices) {
                pkgs[i] = list[i].pkgName
                apps[i] = list[i].appName
            }
            return AppInfo(apps[0], TextUtils.join(",", apps), pkgs)
        }

        fun getIcon(ctx: Context, pkgName: String): Drawable? {
            return getIcon(ctx, pkgName, false)
        }

        @Synchronized
        fun getIcon(ctx: Context, pkgName: String, onlyPeek: Boolean): Drawable? {
            var drawable: Drawable? = null
            synchronized(AppInfo::class.java) {
                var iconInfo: IconInfo
                if (defaultIcon == null) {
                    defaultIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_android)
                }
                val pm = ctx.packageManager
                var appPackageInfo: PackageInfo? = null
                try {
                    appPackageInfo = pm.getPackageInfo(pkgName, 0)
                    val lastUpdate = appPackageInfo.lastUpdateTime
                    iconInfo = iconCache[pkgName] as IconInfo
                    if (iconInfo.date == lastUpdate && iconInfo.icon != null) {
                        drawable = iconInfo.icon
                    }
                } catch (ignore: PackageManager.NameNotFoundException) {
                }
                if (appPackageInfo != null) {
                    if (!onlyPeek) {
                        drawable = appPackageInfo.applicationInfo.loadIcon(pm)
                        iconInfo = IconInfo()
                        iconInfo.date = appPackageInfo.lastUpdateTime
                        iconInfo.icon = drawable
                        iconCache.put(pkgName, iconInfo)
                    }
                } else {
                    iconCache.remove(pkgName)
                    drawable = defaultIcon
                }
            }
            return drawable
        }
    }


}