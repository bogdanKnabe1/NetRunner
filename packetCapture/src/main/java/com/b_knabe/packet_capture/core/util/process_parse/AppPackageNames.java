package com.b_knabe.packet_capture.core.util.process_parse;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

public class AppPackageNames implements Parcelable, Serializable {

    public static final Creator<AppPackageNames> CREATOR = new Creator<AppPackageNames>() {
        @Override
        public AppPackageNames createFromParcel(Parcel in) {
            return new AppPackageNames(in);
        }

        @Override
        public AppPackageNames[] newArray(int size) {
            return new AppPackageNames[size];
        }
    };

    private final String[] packages;

    private AppPackageNames(String[] packages) {
        this.packages = packages;
    }

    private AppPackageNames(Parcel in) {
        this.packages = new String[in.readInt()];
        in.readStringArray(this.packages);
    }

    public static AppPackageNames newInstance(String[] packages) {
        return new AppPackageNames(packages);
    }

    public static AppPackageNames newInstanceFromCommaList(String pkgList) {
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

