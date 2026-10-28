package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.domain.model.ReceiptScanDraft
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

class ReceiptOcrScanner(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanReceipt(uri: Uri): ReceiptScanDraft = withContext(Dispatchers.Default) {
        val image = InputImage.fromFilePath(context, uri)
        val visionText = processImageAsync(image)
        parseReceiptText(visionText?.text ?: "")
    }

    suspend fun scanReceipt(bitmap: Bitmap): ReceiptScanDraft = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = processImageAsync(image)
        parseReceiptText(visionText?.text ?: "")
    }

    private suspend fun processImageAsync(image: InputImage): Text? =
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    cont.resume(text)
                }
                .addOnFailureListener { _ ->
                    cont.resume(null)
                }
        }

    /**
     * Parser lokal untuk struk kasir Indonesia (Indomaret, Alfamart, Restoran, dll.)
     * Mengekstrak kandidat merchant, tanggal, total, dan subtotal
     */
    fun parseReceiptText(rawText: String): ReceiptScanDraft {
        if (rawText.isBlank()) {
            return ReceiptScanDraft(null, null, null, null, "")
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Ekstrak Merchant (biasanya 1-3 baris teratas)
        val merchantCandidate = extractMerchant(lines)

        // 2. Ekstrak Tanggal
        val dateCandidate = extractDate(lines)

        // 3. Ekstrak Nominal Total & Subtotal
        val (total, subtotal) = extractAmounts(lines)

        return ReceiptScanDraft(
            merchant = merchantCandidate,
            dateMillis = dateCandidate,
            totalAmount = total,
            subtotalAmount = subtotal,
            rawText = rawText
        )
    }

    private fun extractMerchant(lines: List<String>): String? {
        val knownChains = listOf("indomaret", "alfamart", "alfamidi", "superindo", "hypermart", "starbucks", "mcdonald", "kfc", "pertamina")
        for (line in lines.take(5)) {
            val lower = line.lowercase()
            for (chain in knownChains) {
                if (lower.contains(chain)) {
                    return chain.replaceFirstChar { it.uppercase() }
                }
            }
        }
        // Jika tidak ada chain terkenal, ambil baris pertama yang bukan angka murni
        return lines.firstOrNull { line ->
            line.length in 3..35 && !line.all { it.isDigit() || it.isWhitespace() || it == '-' || it == '/' }
        }
    }

    private fun extractDate(lines: List<String>): Long? {
        // Regex format tanggal: dd/MM/yyyy atau dd-MM-yyyy atau dd.MM.yyyy
        val datePattern = Pattern.compile("(\\b\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{2,4}\\b)")
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        )

        for (line in lines) {
            val matcher = datePattern.matcher(line)
            if (matcher.find()) {
                val dateStr = matcher.group(1) ?: continue
                for (fmt in formats) {
                    try {
                        val parsed = fmt.parse(dateStr)
                        if (parsed != null && parsed.time > 0) {
                            return parsed.time
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return null
    }

    private fun extractAmounts(lines: List<String>): Pair<Long?, Long?> {
        var totalAmount: Long? = null
        var subtotalAmount: Long? = null

        // Pola nominal: Rp 12.500 atau 12,500 atau 12500
        val amountPattern = Pattern.compile("(?:rp\\.?)?\\s*([0-9]{1,3}(?:[\\.,][0-9]{3})*(?:[\\.,][0-9]{2})?|[0-9]{3,})", Pattern.CASE_INSENSITIVE)

        for (line in lines) {
            val lower = line.lowercase()
            if (lower.contains("total") || lower.contains("grand total") || lower.contains("jumlah")) {
                val matcher = amountPattern.matcher(line)
                var lastFound: Long? = null
                while (matcher.find()) {
                    val rawNum = matcher.group(1) ?: continue
                    val clean = parseRupiahLong(rawNum)
                    if (clean != null && clean > 0) {
                        lastFound = clean
                    }
                }
                if (lastFound != null && totalAmount == null) {
                    totalAmount = lastFound
                }
            } else if (lower.contains("subtotal") || lower.contains("sub total")) {
                val matcher = amountPattern.matcher(line)
                while (matcher.find()) {
                    val rawNum = matcher.group(1) ?: continue
                    val clean = parseRupiahLong(rawNum)
                    if (clean != null && clean > 0) {
                        subtotalAmount = clean
                    }
                }
            }
        }

        // Jika tidak ditemukan label "total", cari angka terbesar yang masuk akal
        if (totalAmount == null) {
            var maxFound = 0L
            for (line in lines) {
                val matcher = amountPattern.matcher(line)
                while (matcher.find()) {
                    val rawNum = matcher.group(1) ?: continue
                    val clean = parseRupiahLong(rawNum)
                    if (clean != null && clean in 1000..50000000 && clean > maxFound) {
                        maxFound = clean
                    }
                }
            }
            if (maxFound > 0L) {
                totalAmount = maxFound
            }
        }

        return Pair(totalAmount, subtotalAmount)
    }

    private fun parseRupiahLong(raw: String): Long? {
        val sanitized = raw.replace("Rp", "", ignoreCase = true)
            .replace(" ", "")
            .trim()

        return try {
            // Jika ada pemisah ribuan titik (misal 12.500)
            if (sanitized.contains(".") && !sanitized.contains(",")) {
                sanitized.replace(".", "").toLong()
            } else if (sanitized.contains(",") && !sanitized.contains(".")) {
                sanitized.replace(",", "").toLong()
            } else if (sanitized.contains(".") && sanitized.contains(",")) {
                // misal 12.500,00 -> hilangkan desimal
                val beforeComma = sanitized.substringBefore(",")
                beforeComma.replace(".", "").toLong()
            } else {
                sanitized.toLong()
            }
        } catch (_: Exception) {
            null
        }
    }
}
