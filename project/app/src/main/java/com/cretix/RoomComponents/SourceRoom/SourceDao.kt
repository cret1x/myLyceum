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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(source: VKSourceEntity)


    @Query("UPDATE vksourceentity SET is_selected = :state WHERE id = :id")
    fun updateSelectedState(id: String, state: Boolean)

    @Query("DELETE FROM vksourceentity")
    fun clear(): Completable

    @Delete
    fun delete(source: VKSourceEntity)
}



@Dao
interface FacebookSourceDao {
    @Query("SELECT * FROM facebooksourceentity")
    fun getAll(): Flowable<List<FacebookSourceEntity>>

    @Query("SELECT * FROM facebooksourceentity WHERE id = :id")
    fun getById(id: String): Single<FacebookSourceEntity>

    @Query("SELECT * FROM facebooksourceentity WHERE is_selected = 1")
    fun getSelected(): Flowable<List<FacebookSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(source: List<FacebookSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(source: FacebookSourceEntity)

    @Update
    fun update(source: FacebookSourceEntity)

    @Update
    fun updateAll(vararg sources: FacebookSourceEntity)

    @Query("UPDATE facebooksourceentity SET is_selected = :state WHERE id = :id")
    fun updateSelectedState(id: String, state: Boolean)

    @Delete
    fun delete(source: FacebookSourceEntity)

    @Query("DELETE FROM facebooksourceentity")
    fun clear(): Completable
}


@Dao
interface TwitterSourceDao {
    @Query("SELECT * FROM twittersourceentity")
    fun getAll(): Flowable<List<TwitterSourceEntity>>

    @Query("SELECT * FROM twittersourceentity WHERE id = :id")
    fun getById(id: String): Single<TwitterSourceEntity>

    @Query("SELECT * FROM twittersourceentity WHERE is_selected = 1")
    fun getSelected(): Flowable<List<TwitterSourceEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(source: TwitterSourceEntity)

    @Query("UPDATE twittersourceentity SET is_selected = :state WHERE id = :id")
    fun updateSelectedState(id: String, state: Boolean)

    @Delete
    fun delete(source: TwitterSourceEntity)

    @Query("DELETE FROM twittersourceentity")
    fun clear(): Completable
}