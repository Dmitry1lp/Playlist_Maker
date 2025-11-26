package com.practicum.playlistmaker.media.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = 2, entities = [TrackEntity::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE track_table ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}