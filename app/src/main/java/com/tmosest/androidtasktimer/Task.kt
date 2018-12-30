package com.tmosest.androidtasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name: String, val description: String, val sortOrder: Int) : Parcelable {
    val id: Long = 0
}
