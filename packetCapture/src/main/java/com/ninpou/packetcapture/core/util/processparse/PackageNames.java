package com.ninpou.packetcapture.core.util.processparse;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

public class PackageNames implements Parcelable, Serializable {
    public static final Creator<PackageNames> CREATOR = new Creator<PackageNames>() {
        @Override
        public PackageNames createFromParcel(Parcel in) {
            return new PackageNames(in);
        }

        @Override
        public PackageNames[] newArray(int size) {
            return new PackageNames[size];
        }
    };
    private final String[] packages;

    private PackageNames(String[] packages) {
        this.packages = packages;
    }

    private PackageNames(Parcel in) {
        this.packages = new String[in.readInt()];
        in.readStringArray(this.packages);
    }

    public static PackageNames newInstance(String[] packages) {
        return new PackageNames(packages);
    }

    public static PackageNames newInstanceFromCommaList(String pkgList) {
        return newInstance(pkgList.split(","));
    }

    public String getAt(int i) {
        if (packages.length > i) {
            return packages[i];
        }
        return null;
    }

    public String getCommaJoinedString() {
        return TextUtils.join(",", packages);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(packages.length);
        dest.writeStringArray(packages);
    }
}

