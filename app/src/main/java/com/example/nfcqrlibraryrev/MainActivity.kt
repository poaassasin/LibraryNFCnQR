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

        Toast.makeText(this, "NFC Tag Ditemukan!", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            dataRepository.processAndSaveNfcIntent(intent)
        }
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