package com.practicum.playlistmaker.media.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = 4, entities = [TrackEntity::class, PlaylistEntity::class, TrackFavoriteEntity::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun playlistDao(): PlaylistDao
    abstract fun trackFavoriteDao(): TrackFavoriteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track_table ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            CREATE TABLE playlist_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                filePath TEXT,
                tracksId TEXT NOT NULL,
                trackCount INTEGER NOT NULL
            )
        """)

                database.execSQL("""
            CREATE TABLE playlist_track_table (
                trackId INTEGER PRIMARY KEY NOT NULL,
                previewUrl TEXT,
                collectionName TEXT,
                releaseDate TEXT,
                primaryGenreName TEXT,
                country TEXT,
                trackName TEXT,
                artistName TEXT,
                trackTimeMillis INTEGER NOT NULL,
                artworkUrl100 TEXT,
                addedAt INTEGER NOT NULL
            )
        """)
            }
        }
    }
}