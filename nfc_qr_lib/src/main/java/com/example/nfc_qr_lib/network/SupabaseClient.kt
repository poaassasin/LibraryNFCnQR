package com.example.nfc_qr_lib.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    // !!! GANTI DENGAN URL & KUNCI SUPABASE ANDA !!!
    private const val SUPABASE_URL = "https://ubadhrgbgaaqxxwmpkub.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViYWRocmdiZ2FhcXh4d21wa3ViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI0NTc0MzIsImV4cCI6MjA2ODAzMzQzMn0.rXZWL2R98hjT3d61Bs3SayDO2n4yJLqZpjj-1i3Ms98"
    // !!! GANTI DI ATAS !!!

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}