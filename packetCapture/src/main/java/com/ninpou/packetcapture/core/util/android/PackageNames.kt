package com.ninpou.packetcapture.core.util.android

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import java.io.Serializable

class PackageNames : Parcelable, Serializable {
    private val packages: Array<String?>

    private constructor(packages: Array<String?>) {
        this.packages = packages
    }

    private constructor(`in`: Parcel) {
        packages = arrayOfNulls(`in`.readInt())
        `in`.readStringArray(packages)
    }

    fun getAt(i: Int): String? {
        return if (packages.size > i) {
            packages[i]
        } else null
    }

    val commaJoinedString: String
        get() = TextUtils.join(",", packages)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(packages.size)
        dest.writeStringArray(packages)
    }

    companion object {
        val CREATOR: Parcelable.Creator<PackageNames?> = object : Parcelable.Creator<PackageNames?> {
            override fun createFromParcel(`in`: Parcel): PackageNames? {
                return PackageNames(`in`)
            }

            override fun newArray(size: Int): Array<PackageNames?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun newInstance(packages: Array<String?>): PackageNames {
            return PackageNames(packages)
        }

        fun newInstanceFromCommaList(pkgList: String): PackageNames {
            return newInstance(pkgList.split(",").toTypedArray())
        }
    }
}