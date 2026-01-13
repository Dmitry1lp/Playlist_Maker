package com.practicum.playlistmaker.media.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackFavoriteEntity)

    @Query("DELETE FROM playlist_track_table WHERE trackId = :trackId")
    suspend fun deleteTrackEntity(trackId: Long)

    @Query("SELECT * FROM playlist_track_table WHERE trackId IN (:ids)")
    suspend fun getTracksByIds(ids: List<Long>): List<TrackFavoriteEntity>

    @Query("SELECT * FROM playlist_track_table ORDER BY addedAt DESC")
    fun getFavoriteTracks(): Flow<List<TrackFavoriteEntity>>

    @Query("DELETE FROM playlist_track_table")
    suspend fun clearAll()
}