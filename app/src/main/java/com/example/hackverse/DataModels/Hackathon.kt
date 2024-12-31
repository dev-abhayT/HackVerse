package com.example.hackverse.DataModels

import android.os.Parcel
import android.os.Parcelable


data class Hackathon(
    val name: String = "",
    val location: String = "",
    val date: String = "",
    val bannerUrl: String = "",
    val details: String = "",
    val hackID: String = "",
    val upvotes: Int = 0,
    val comments: Int = 0,
    val registrations: Int = 0,
    val createdByUserName: String ="",
    val createdByUserID: String ="") : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(location)
        parcel.writeString(date)
        parcel.writeString(bannerUrl)
        parcel.writeString(details)
        parcel.writeString(hackID)
        parcel.writeInt(upvotes)
        parcel.writeInt(comments)
        parcel.writeInt(registrations)
        parcel.writeString(createdByUserID)
        parcel.writeString(createdByUserName)


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
    }}
