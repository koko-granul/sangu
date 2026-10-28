package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.data.local.entity.AttachmentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaManager(private val context: Context) {

    private val attachmentsDir = File(context.filesDir, "transaction_attachments").apply { mkdirs() }
    private val thumbnailsDir = File(context.filesDir, "transaction_thumbnails").apply { mkdirs() }

    /**
     * Memproses URI gambar dari kamera atau galeri:
     * - Memperbaiki orientasi EXIF
     * - Menormalkan format menjadi JPEG murni
     * - Menghilangkan metadata sensitif (lokasi/EXIF)
     * - Membuat thumbnail terpisah untuk daftar transaksi
     * - Mengembalikan AttachmentEntity dengan path relatif
     */
    suspend fun processAndSaveImage(
        uri: Uri,
        purpose: String = "RECEIPT"
    ): AttachmentEntity = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Tidak dapat membaca data gambar")

        // Membaca EXIF orientasi dari stream sementara
        val tempExifFile = File(context.cacheDir, "temp_exif_$id")
        tempExifFile.outputStream().use { out -> inputStream.copyTo(out) }

        val exif = ExifInterface(tempExifFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationAngle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        // Decode bitmap
        val originalBitmap = BitmapFactory.decodeFile(tempExifFile.absolutePath)
        tempExifFile.delete()

        // Rotasi piksel jika ada offset orientasi
        val correctedBitmap = if (rotationAngle != 0f) {
            val matrix = Matrix().apply { postRotate(rotationAngle) }
            Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        } else {
            originalBitmap
        }

        // 1. Simpan JPEG resolusi penuh (maksimal dimensi 2560px untuk struk agar tajam)
        val maxDim = 2560
        val scale = if (correctedBitmap.width > maxDim || correctedBitmap.height > maxDim) {
            val ratio = maxDim.toFloat() / maxOf(correctedBitmap.width, correctedBitmap.height)
            ratio
        } else 1.0f

        val finalBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                correctedBitmap,
                (correctedBitmap.width * scale).toInt(),
                (correctedBitmap.height * scale).toInt(),
                true
            )
        } else {
            correctedBitmap
        }

        val relativeFileName = "att_$id.jpg"
        val finalFile = File(attachmentsDir, relativeFileName)
        FileOutputStream(finalFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        // 2. Buat Thumbnail (maksimal 400px)
        val thumbScale = 400f / maxOf(finalBitmap.width, finalBitmap.height).toFloat()
        val thumbBitmap = if (thumbScale < 1.0f) {
            Bitmap.createScaledBitmap(
                finalBitmap,
                (finalBitmap.width * thumbScale).toInt().coerceAtLeast(1),
                (finalBitmap.height * thumbScale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            finalBitmap
        }

        val thumbRelativeFileName = "thumb_$id.jpg"
        val thumbFile = File(thumbnailsDir, thumbRelativeFileName)
        FileOutputStream(thumbFile).use { out ->
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        val relativePath = "transaction_attachments/$relativeFileName"
        val thumbRelativePath = "transaction_thumbnails/$thumbRelativeFileName"

        AttachmentEntity(
            id = id,
            transactionId = 0L, // dihubungkan saat transaksi disimpan
            relativePath = relativePath,
            thumbnailRelativePath = thumbRelativePath,
            purpose = purpose,
            mimeType = "image/jpeg",
            width = finalBitmap.width,
            height = finalBitmap.height,
            byteSize = finalFile.length()
        )
    }

    fun getFileFromRelativePath(relativePath: String): File {
        return File(context.filesDir, relativePath)
    }

    fun deleteAttachmentFiles(attachment: AttachmentEntity) {
        try {
            File(context.filesDir, attachment.relativePath).delete()
            File(context.filesDir, attachment.thumbnailRelativePath).delete()
        } catch (_: Exception) {
        }
    }
}
