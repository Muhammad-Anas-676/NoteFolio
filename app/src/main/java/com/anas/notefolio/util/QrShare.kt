package com.anas.notefolio.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrShare {
    /** Max characters mirrors the original app's cutoff so a QR stays scannable at EC level L. */
    const val MAX_CHARS = 800

    fun buildPayload(title: String, body: String): Pair<String, Boolean> {
        val full = if (title.isBlank()) body else "$title\n\n$body"
        val truncated = full.length > MAX_CHARS
        return Pair(if (truncated) full.take(MAX_CHARS) else full, truncated)
    }

    fun generate(text: String, sizePx: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }
}
