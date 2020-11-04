package com.cretix.RoomComponents.SourceRoom

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VKSourceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "gid") val gid: Long,
    @ColumnInfo(name = "is_closed") val isClosed: Boolean,
    @ColumnInfo(name = "group_icon") val groupIcon: String,
    @ColumnInfo(name = "is_selected") val isSelected: Boolean,
    @ColumnInfo(name = "is_notify") val isNotify: Boolean
)