package com.example.nfc_qr_lib.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nfc_qr_lib.local.OfflineDatabase
import com.example.nfc_qr_lib.network.NfcDataPayload
import com.example.nfc_qr_lib.network.QrDataPayload
import com.example.nfc_qr_lib.network.SupabaseClient

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val syncDao = OfflineDatabase.getDatabase(appContext).syncDao()
    private val apiService = SupabaseClient.instance

    override suspend fun doWork(): Result {
        try {
            val unsyncedNfc = syncDao.getUnsyncedNfcData()
            unsyncedNfc.forEach { dataOffline ->
                val payload = NfcDataPayload(dataOffline.tag_id, dataOffline.content, dataOffline.timestamp, dataOffline.tag_type)
                val response = apiService.insertNfcData(
                    apiKey = SupabaseClient.SUPABASE_ANON_KEY,
                    bearerToken = "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}",
                    data = payload
                )
                if (response.isSuccessful) {
                    syncDao.markNfcDataAsSynced(dataOffline.localId)
                }
            }

            val unsyncedQr = syncDao.getUnsyncedQrData()
            unsyncedQr.forEach { dataOffline ->
                val payload = QrDataPayload(dataOffline.content, dataOffline.generated_at, dataOffline.format)
                val response = apiService.insertQrData(
                    apiKey = SupabaseClient.SUPABASE_ANON_KEY,
                    bearerToken = "Bearer ${SupabaseClient.SUPABASE_ANON_KEY}",
                    data = payload
                )
                if (response.isSuccessful) {
                    syncDao.markQrDataAsSynced(dataOffline.localId)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}