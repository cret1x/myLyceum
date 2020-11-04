package com.cretix.RoomComponents.PostRoom

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface VKPostDao {
    @Query("SELECT * FROM vkpostentity WHERE is_favourite = 1")
    fun getFavourite(): Flowable<List<VKPostEntity>>

    @Insert
    fun addFavourite(post: VKPostEntity): Completable

    @Query("SELECT is_favourite FROM vkpostentity WHERE id = :pid")
    fun isFavourite(pid: String) : Flowable<Boolean>

    @Query("DELETE FROM vkpostentity WHERE id = :pid")
    fun removeFavourite(pid: String): Completable
}