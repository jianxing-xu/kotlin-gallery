package cn.xu.gallery

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Pixabay(
    @SerializedName("total_results")
    val totalHits: Int,
    @SerializedName("photos")
    val hits: Array<PhotoItem>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pixabay

        if (totalHits != other.totalHits) return false
        if (!hits.contentEquals(other.hits)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalHits
        result = 31 * result + hits.contentHashCode()
        return result
    }
}


data class PhotoItem(
    @SerializedName(value = "id")
    val photoId: Int,
    @SerializedName("src")
    val src: Src?,
    @SerializedName("height")
    val height: Int,
    @SerializedName("photographer")
    val name: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Src::class.java.classLoader),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(photoId)
        parcel.writeParcelable(src, flags)
        parcel.writeInt(height)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhotoItem> {
        override fun createFromParcel(parcel: Parcel): PhotoItem {
            return PhotoItem(parcel)
        }

        override fun newArray(size: Int): Array<PhotoItem?> {
            return arrayOfNulls(size)
        }
    }

}

data class Src(
    @SerializedName("small")
    val previewUrl: String?,
    @SerializedName("medium")
    val fullUrl: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Src> {
        override fun createFromParcel(parcel: Parcel): Src {
            return Src(parcel)
        }

        override fun newArray(size: Int): Array<Src?> {
            return arrayOfNulls(size)
        }
    }
}