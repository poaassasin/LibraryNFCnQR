// file: nfc_qr_lib/src/main/java/com/yourdomain/nfc_qr_lib/network/DataModel.kt
package com.example.nfc_qr_lib.network

/**
 * Merepresentasikan data yang akan dikirim ke tabel 'nfc_data' di Supabase.
 * Nama properti harus cocok persis dengan nama kolom di tabel Anda.
 */
data class NfcDataPayload(
    val tag_id: String,
    val content: String,
    val timestamp: String,
    val tag_type: String
)

/**
 * Merepresentasikan data yang akan dikirim ke tabel 'qr_data' di Supabase.
 */
data class QrDataPayload(
    val content: String,
    val generated_at: String,
    val format: String = "QR_CODE" // Bisa diberi nilai default
)