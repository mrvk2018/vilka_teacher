package com.koreanimmersion.service

import android.os.Parcel
import android.os.Parcelable
import com.koreanimmersion.domain.playback.PlaybackSegment
import com.koreanimmersion.domain.playback.PlaybackSegmentType

/** Parcelable wrapper для передачи очереди в Service через Intent. */
data class PlaybackSegmentParcelable(
    val typeName: String,
    val audioUrl: String?,
    val displayText: String?,
    val phraseId: Long?,
    val pauseDurationMs: Long
) : Parcelable {

    fun toDomain(): PlaybackSegment = PlaybackSegment(
        type = PlaybackSegmentType.valueOf(typeName),
        audioUrl = audioUrl,
        displayText = displayText,
        phraseId = phraseId,
        pauseDurationMs = pauseDurationMs
    )

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(typeName)
        parcel.writeString(audioUrl)
        parcel.writeString(displayText)
        parcel.writeValue(phraseId)
        parcel.writeLong(pauseDurationMs)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PlaybackSegmentParcelable> {
        override fun createFromParcel(parcel: Parcel): PlaybackSegmentParcelable =
            PlaybackSegmentParcelable(parcel)

        override fun newArray(size: Int): Array<PlaybackSegmentParcelable?> = arrayOfNulls(size)

        fun fromDomain(segment: PlaybackSegment): PlaybackSegmentParcelable =
            PlaybackSegmentParcelable(
                typeName = segment.type.name,
                audioUrl = segment.audioUrl,
                displayText = segment.displayText,
                phraseId = segment.phraseId,
                pauseDurationMs = segment.pauseDurationMs
            )
    }
}
