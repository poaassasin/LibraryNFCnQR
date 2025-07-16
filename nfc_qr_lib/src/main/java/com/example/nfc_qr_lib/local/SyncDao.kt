package com.example.nfc_qr_lib.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNfcData(data: NfcDataOffline)

    @Query("SELECT * FROM nfc_data_offline WHERE isSynced = 0")
    suspend fun getUnsyncedNfcData(): List<NfcDataOffline>

    @Query("UPDATE nfc_data_offline SET isSynced = 1 WHERE localId = :localId")
    suspend fun markNfcDataAsSynced(localId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQrData(data: QrDataOffline)

    @Query("SELECT * FROM qr_data_offline WHERE isSynced = 0")
    suspend fun getUnsyncedQrData(): List<QrDataOffline>

    @Query("UPDATE qr_data_offline SET isSynced = 1 WHERE localId = :localId")
    suspend fun markQrDataAsSynced(localId: Int)
}