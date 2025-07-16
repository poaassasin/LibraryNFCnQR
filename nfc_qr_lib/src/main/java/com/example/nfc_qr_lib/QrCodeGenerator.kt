package com.example.nfc_qr_lib

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Menghasilkan Bitmap QR Code dari sebuah teks.
 *
 * @param content Teks yang akan diubah menjadi QR Code.
 * @param size Ukuran gambar QR Code dalam piksel.
 * @return Bitmap QR Code, atau null jika terjadi error.
 */
fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Sebuah Composable untuk menampilkan gambar QR Code dengan mudah.
 *
 * @param content Teks yang akan ditampilkan sebagai QR Code.
 * @param modifier Modifier untuk kustomisasi layout.
 */
@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    generateQrBitmap(content)?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "QR Code for $content",
            modifier = modifier
        )
    }
}