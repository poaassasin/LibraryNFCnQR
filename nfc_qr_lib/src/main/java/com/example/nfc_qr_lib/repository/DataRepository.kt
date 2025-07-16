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
import android.content.Intent // Tambahkan import
import android.nfc.NfcAdapter // Tambahkan import
import android.nfc.Tag // Tambahkan import
import android.os.Build // Tambahkan import
import com.example.nfc_qr_lib.NfcReader // Tambahkan import

class DataRepository(context: Context) {

    private val syncDao = OfflineDatabase.getDatabase(context).syncDao()
    private val apiService = SupabaseClient.instance
    private val workManager = WorkManager.getInstance(context)

    suspend fun processAndSaveNfcIntent(intent: Intent) {
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        // Jika tidak ada tag, hentikan proses
        if (tag == null) return

        // Ambil info teknis yang pasti ada
        val tagId = tag.id.joinToString(":") { "%02X".format(it) }
        val tagType = tag.techList.firstOrNull() ?: "Unknown"

        // Coba baca konten NDEF menggunakan NfcReader dari library kita
        val nfcContent = NfcReader.processNfcIntent(intent)

        // Tentukan konten yang akan disimpan
        val finalContent = if (!nfcContent.isNullOrEmpty()) {
            // Jika ada konten NDEF, gunakan itu
            nfcContent
        } else {
            // Jika tidak ada, gunakan placeholder
            "Proprietary Tag (No NDEF Content)"
        }

        // Buat payload untuk dikirim
        val nfcPayload = NfcDataPayload(
            tag_id = tagId,
            content = finalContent,
            timestamp = System.currentTimeMillis().toString(),
            tag_type = tagType
        )

        // Logika Online-First/Offline-Fallback tetap sama
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