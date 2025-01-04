package com.example.hackverse.DataModels

import android.os.Parcel
import android.os.Parcelable

data class Hackathon(
    val name: String = "",
    val location: String = "",
    val date: String ?= null,
    val bannerUrl: String = "",
    val details: String = "",
    val hackID: String = "",
    val upvotes: Long = 0L,
    val comments: Long = 0L,
    val registrations: Long = 0L,
    val createdByUserName: String = "",
    val createdByUserID: String = "",
    val hackathonDatabaseID: String = "")
 : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(location)
        parcel.writeString(date)
        parcel.writeString(bannerUrl)
        parcel.writeString(details)
        parcel.writeString(hackID)
        parcel.writeLong(upvotes)
        parcel.writeLong(comments)
        parcel.writeLong(registrations)
        parcel.writeString(createdByUserID)
        parcel.writeString(createdByUserName)
        parcel.writeString(hackathonDatabaseID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Hackathon> {
        override fun createFromParcel(parcel: Parcel): Hackathon {
            return Hackathon(parcel)
        }

        override fun newArray(size: Int): Array<Hackathon?> {
            return arrayOfNulls(size)
        }
    }
}
