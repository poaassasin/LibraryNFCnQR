package com.example.nfc_qr_lib.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nfc_qr_lib.local.NfcDataOffline
import com.example.nfc_qr_lib.local.OfflineDatabase
import com.example.nfc_qr_lib.local.QrDataOffline
import com.example.nfc_qr_lib.network.NfcDataPayload
import com.example.nfc_qr_lib.network.QrDataPayload
import com.example.nfc_qr_lib.network.SupabaseClient
import com.example.nfc_qr_lib.worker.SyncWorker
import java.util.concurrent.TimeUnit

class DataRepository(context: Context) {

    private val syncDao = OfflineDatabase.getDatabase(context).syncDao()
    private val apiService = SupabaseClient.instance
    private val workManager = WorkManager.getInstance(context)

    suspend fun saveNfcData(tagId: String, content: String, tagType: String) {
        val nfcPayload = NfcDataPayload(
            tag_id = tagId,
            content = content,
            timestamp = System.currentTimeMillis().toString(),
            tag_type = tagType
        )
        try {
            val response = apiService.insertNfcData(
                apiKey = SupabaseClient.SUPABASE_ANON_KEY,
                bearerToken = "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}",
                data = nfcPayload
            )
            if (!response.isSuccessful) {
                saveNfcToLocal(nfcPayload)
            }
        } catch (e: Exception) {
            println("Koneksi gagal, menyimpan data NFC ke lokal. Error: ${e.message}")
            saveNfcToLocal(nfcPayload)
        }
    }

    suspend fun saveQrData(content: String, format: String = "QR_CODE") {
        val qrPayload = QrDataPayload(
            content = content,
            generated_at = System.currentTimeMillis().toString(),
            format = format
        )
        try {
            val response = apiService.insertQrData(
                apiKey = SupabaseClient.SUPABASE_ANON_KEY,
                bearerToken = "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}",
                data = qrPayload
            )
            if (!response.isSuccessful) {
                saveQrToLocal(qrPayload)
            }
        } catch (e: Exception) {
            println("Koneksi gagal, menyimpan data QR ke lokal. Error: ${e.message}")
            saveQrToLocal(qrPayload)
        }
    }

    private suspend fun saveNfcToLocal(payload: NfcDataPayload) {
        val offlineData = NfcDataOffline(
            tag_id = payload.tag_id,
            content = payload.content,
            timestamp = payload.timestamp,
            tag_type = payload.tag_type,
            isSynced = false
        )
        syncDao.insertNfcData(offlineData)
        scheduleSync()
    }

    private suspend fun saveQrToLocal(payload: QrDataPayload) {
        val offlineData = QrDataOffline(
            content = payload.content,
            generated_at = payload.generated_at,
            format = payload.format,
            isSynced = false
        )
        syncDao.insertQrData(offlineData)
        scheduleSync()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "sync_data_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}