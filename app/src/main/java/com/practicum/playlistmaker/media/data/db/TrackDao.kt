package com.practicum.playlistmaker.media.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrackEntity(track: TrackEntity)

    @Query("SELECT * FROM track_table ORDER BY addedAt DESC")
    fun getTracks(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM track_table")
    suspend fun getTracksId(): List<Long>

    @Query("DELETE FROM track_table")
    suspend fun clearAll()

}