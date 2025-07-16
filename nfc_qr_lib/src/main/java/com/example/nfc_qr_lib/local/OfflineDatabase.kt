package com.example.nfc_qr_lib.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NfcDataOffline::class, QrDataOffline::class], version = 1, exportSchema = false)
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getDatabase(context: Context): OfflineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "offline_sync_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}