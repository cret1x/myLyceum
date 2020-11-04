package com.cretix.RoomComponents.SourceRoom

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface VKSourceDao {
    @Query("SELECT * FROM vksourceentity")
    fun getAll(): Flowable<List<VKSourceEntity>>

    @Query("SELECT * FROM vksourceentity WHERE id = :id")
    fun getById(id: String): Single<VKSourceEntity>

    @Query("SELECT title FROM vksourceentity WHERE gid = :gid")
    fun getTitleByGid(gid: Long): String

    @Query("SELECT * FROM vksourceentity WHERE is_selected = 1")
    fun getSelected(): Flowable<List<VKSourceEntity>>

    @Query("SELECT * FROM vksourceentity WHERE is_selected = 0")
    fun getNotSelected(): Flowable<List<VKSourceEntity>>

    @Query("SELECT gid FROM vksourceentity WHERE is_notify = 1")
    fun getNotify(): Flowable<List<Long>>

    @Query("SELECT gid FROM vksourceentity WHERE is_notify = 0")
    fun getNotNotify(): Flowable<List<Long>>

    @Query("SELECT group_icon FROM vksourceentity WHERE gid = :gid")
    fun getIconById(gid: Long): String

    @Query("UPDATE vksourceentity SET is_notify = :state WHERE id = :id")
    fun updateNotificationState(id: String, state: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(source: VKSourceEntity)

    @Query("UPDATE vksourceentity SET title = :title, is_closed = :isClosed, group_icon = :groupIcon WHERE id = :id")
    fun update(id: String, title: String, isClosed: Boolean, groupIcon: String)

    @Query("SELECT EXISTS(SELECT * FROM vksourceentity WHERE id = :id)")
    fun isRowIsExist(id : String) : Boolean


    @Query("UPDATE vksourceentity SET is_selected = :state WHERE id = :id")
    fun updateSelectedState(id: String, state: Boolean)

    @Query("DELETE FROM vksourceentity")
    fun clear(): Completable

    @Query("DELETE FROM vksourceentity WHERE id = :id")
    fun deleteById(id: String): Completable

    @Delete
    fun delete(source: VKSourceEntity)
}
