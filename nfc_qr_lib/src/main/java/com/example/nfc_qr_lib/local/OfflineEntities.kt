package com.example.nfc_qr_lib.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfc_data_offline")
data class NfcDataOffline(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val tag_id: String,
    val content: String,
    val timestamp: String,
    val tag_type: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "qr_data_offline")
data class QrDataOffline(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val content: String,
    val generated_at: String,
    val format: String = "QR_CODE",
    val isSynced: Boolean = false
)