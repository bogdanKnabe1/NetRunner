package com.ninpou.packetcapture.core.util.processparse

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class PackageNames : Parcelable, Serializable {
    @JvmField
    val pkgs: Array<String?>

    protected constructor(pkgs: Array<String?>) {
        this.pkgs = pkgs
    }

    fun getAt(i: Int): String? {
        return if (pkgs.size > i) {
            pkgs[i]
        } else null
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(pkgs.size)
        dest.writeStringArray(pkgs)
    }

    protected constructor(`in`: Parcel) {
        pkgs = arrayOfNulls(`in`.readInt())
        `in`.readStringArray(pkgs)
    }

    companion object {
        @JvmStatic
        fun newInstance(pkgs: Array<String?>): PackageNames {
            return PackageNames(pkgs)
        }

        val CREATOR: Parcelable.Creator<PackageNames> = object : Parcelable.Creator<PackageNames> {
            override fun createFromParcel(`in`: Parcel): PackageNames? {
                return PackageNames(`in`)
            }

            override fun newArray(size: Int): Array<PackageNames?> {
                return arrayOfNulls(size)
            }
        }
    }
}