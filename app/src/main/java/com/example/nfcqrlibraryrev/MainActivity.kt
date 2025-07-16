package com.example.nfcqrlibraryrev // Ganti dengan package name aplikasi Anda

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.nfc_qr_lib.NfcReader
import com.example.nfc_qr_lib.QrCodeImage
import com.example.nfc_qr_lib.repository.DataRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var dataRepository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        dataRepository = DataRepository(applicationContext)

        setContent {
            // Panggil UI utama aplikasi Anda
            MainAppScreen(dataRepository)
        }
    }

    // Logika untuk menangani NFC saat aplikasi berada di foreground
    override fun onResume() {
        super.onResume()
        nfcAdapter?.let {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
            it.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // Fungsi ini dipanggil saat tag NFC baru terdeteksi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Cukup periksa apakah ada 'Tag' yang terdeteksi dari intent
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag != null) {
            // JIKA SAMPAI SINI, ARTINYA SCAN BERHASIL!
            Toast.makeText(this, "Scan Tag NFC Berhasil!", Toast.LENGTH_SHORT).show()

            // Ambil data teknis yang pasti ada
            val tagId = tag.id.joinToString(":") { "%02X".format(it) }
            val tagType = tag.techList.firstOrNull() ?: "Unknown"

            // Karena tidak ada konten publik, kita beri placeholder yang jelas
            val contentPlaceholder = "E-Money Card (No NDEF Content)"

            // Simpan bukti scan ke server
            lifecycleScope.launch {
                dataRepository.saveNfcData(tagId, contentPlaceholder, tagType)
            }
        }
        // Tidak perlu blok 'else' karena kita tidak lagi bergantung pada konten
    }
}

@Composable
fun MainAppScreen(repository: DataRepository) {
    var qrContent by remember { mutableStateOf("https://example.com") }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NFC & QR Library Test", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(32.dp))

            // Tampilkan QR Code
            QrCodeImage(content = qrContent, modifier = Modifier.size(250.dp))

            Spacer(Modifier.height(16.dp))

            // Tombol untuk menyimpan data QR
            Button(onClick = {
                scope.launch {
                    repository.saveQrData(qrContent)
                }
            }) {
                Text("Simpan Data QR ke Server")
            }

            Spacer(Modifier.height(32.dp))

            Text("Tempelkan Tag NFC untuk menyimpan datanya.")
        }
    }
}