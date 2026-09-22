package com.koreanimmersion.service

import android.os.Parcel
import android.os.Parcelable

data class PhrasePlaybackItemParcelable(
    val phraseId: Long,
    val koreanText: String,
    val russianTranslation: String,
    val englishTranslation: String,
    val audioUrlOrPath: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(phraseId)
        parcel.writeString(koreanText)
        parcel.writeString(russianTranslation)
        parcel.writeString(englishTranslation)
        parcel.writeString(audioUrlOrPath)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PhrasePlaybackItemParcelable> {
        override fun createFromParcel(parcel: Parcel): PhrasePlaybackItemParcelable =
            PhrasePlaybackItemParcelable(parcel)

        override fun newArray(size: Int): Array<PhrasePlaybackItemParcelable?> = arrayOfNulls(size)

        fun fromCoursePhrase(phrase: com.koreanimmersion.core.database.entity.PhraseEntity) =
            PhrasePlaybackItemParcelable(
                phraseId = phrase.id,
                koreanText = phrase.koreanText,
                russianTranslation = phrase.russianTranslation,
                englishTranslation = phrase.englishTranslation,
                audioUrlOrPath = phrase.audioUrlOrPath.takeIf { it.isNotBlank() }
            )
    }
}
