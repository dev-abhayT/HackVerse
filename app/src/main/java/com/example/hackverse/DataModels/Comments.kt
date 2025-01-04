package com.example.hackverse.DataModels

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

data class Comments(
    val commentWriterName : String = "",
    val commentMessage : String = "",
    val commentHackathonName : String = "",
    val commentUserDatabaseID : String = "",
    val commentUserPfpUrl : String = ""
) : Parcelable {

    @SuppressLint("NewApi")
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    @SuppressLint("NewApi")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(commentWriterName)
        parcel.writeString(commentMessage)
        parcel.writeString(commentHackathonName)
        parcel.writeString(commentUserDatabaseID)
        parcel.writeString(commentUserPfpUrl)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comments> {
        override fun createFromParcel(parcel: Parcel): Comments {
            return Comments(parcel)
        }

        override fun newArray(size: Int): Array<Comments?> {
            return arrayOfNulls(size)
        }
    }
}
