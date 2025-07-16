package com.example.nfc_qr_lib

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import java.nio.charset.Charset
import java.util.Arrays

object NfcReader {
    /**
     * Memproses intent NFC. Sekarang bisa menangani NDEF_DISCOVERED
     * dan TECH_DISCOVERED (membaca dari Tag secara manual).
     */
    fun processNfcIntent(intent: Intent): String? {
        // Coba parsing dari intent extra dulu (untuk NDEF_DISCOVERED)
        val rawMessages: Array<NdefMessage?>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
                ?.map { it as NdefMessage }?.toTypedArray()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                ?.map { it as NdefMessage }?.toTypedArray()
        }

        if (rawMessages != null && rawMessages.isNotEmpty()) {
            val parsedText = parseNdefMessage(rawMessages.first())
            if (parsedText != null) return parsedText
        }

        // Jika gagal, coba baca dari Tag mentah (untuk TECH_DISCOVERED)
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag != null) {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val ndefMessage = ndef.ndefMessage
                    ndef.close()
                    if (ndefMessage != null) {
                        return parseNdefMessage(ndefMessage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return null
    }

    private fun parseNdefMessage(message: NdefMessage?): String? {
        message?.records?.forEach { record ->
            if (record.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                try {
                    val payload = record.payload
                    val status = payload[0].toInt()
                    val langCodeLength = status and 0x1F
                    val textEncoding = if ((status and 0x80) == 0) Charsets.UTF_8 else Charsets.UTF_16
                    return String(payload, langCodeLength + 1, payload.size - langCodeLength - 1, textEncoding)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}

// Composable tidak perlu diubah
@Composable
fun NfcResultDisplay(nfcData: String?, modifier: Modifier = Modifier) {
    if (nfcData != null) {
        Text(text = "Data dari NFC: $nfcData", fontWeight = FontWeight.Bold, modifier = modifier)
    } else {
        Text(text = "Tempelkan tag NFC ke belakang ponsel...", modifier = modifier)
    }
}