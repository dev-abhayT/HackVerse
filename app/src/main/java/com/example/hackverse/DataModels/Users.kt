import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

data class Users(
    val name: String = "",
    val userNumber: String = "",
    val pfpUrl: String = "",
    val userID: String = "",
    val userEmail: String = "",


) : Parcelable {

    @SuppressLint("NewApi")
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),


    )

    @SuppressLint("NewApi")
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(userNumber)
        parcel.writeString(pfpUrl)
        parcel.writeString(userID)
        parcel.writeString(userEmail)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Users> {
        override fun createFromParcel(parcel: Parcel): Users {
            return Users(parcel)
        }

        override fun newArray(size: Int): Array<Users?> {
            return arrayOfNulls(size)
        }
    }
}
