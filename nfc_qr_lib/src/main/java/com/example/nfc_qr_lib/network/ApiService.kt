package com.example.nfc_qr_lib.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    /**
     * Mengirim data scan NFC ke tabel 'nfc_data' di Supabase.
     */
    @Headers("Content-Type: application/json")
    @POST("rest/v1/nfc_data")
    suspend fun insertNfcData(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body data: NfcDataPayload
    ): Response<Unit>

    /**
     * Mengirim data QR yang di-generate ke tabel 'qr_data' di Supabase.
     */
    @Headers("Content-Type: application/json")
    @POST("rest/v1/qr_data")
    suspend fun insertQrData(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Body data: QrDataPayload
    ): Response<Unit>
}