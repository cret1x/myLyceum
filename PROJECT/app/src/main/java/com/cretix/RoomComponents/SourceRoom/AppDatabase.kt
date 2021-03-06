package com.cretix.RoomComponents.SourceRoom

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cretix.RoomComponents.PostRoom.VKPostDao
import com.cretix.RoomComponents.PostRoom.VKPostEntity

@Database(entities = arrayOf(VKSourceEntity::class, VKPostEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vkPostDao() : VKPostDao
    abstract fun vkSourceDao(): VKSourceDao
}
