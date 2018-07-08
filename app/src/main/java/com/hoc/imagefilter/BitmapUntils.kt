package com.hoc.imagefilter

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.*
import android.provider.MediaStore.Images.Thumbnails.*

fun Context.getBitmapFromAsset(fileName: String, width: Int, height: Int): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        inSampleSize = calculateInSampleSize(this, width, height)
        inJustDecodeBounds = false
    }
    val inputStream = assets.open(fileName)
    return BitmapFactory.decodeStream(inputStream, null, options)
}

fun Context.getBitmapFromGallery(uri: Uri, width: Int, height: Int): Bitmap? {
    val pathName = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use {
        it.moveToFirst()
        it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
    }
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, this)
        inSampleSize = calculateInSampleSize(this, width, height)
        inJustDecodeBounds = false
    }

    return BitmapFactory.decodeFile(pathName, options)
}

fun ContentResolver.insertImage(source: Bitmap, title: String, description: String): Boolean {
    val values = ContentValues().apply {
        put(TITLE, title)
        put(DISPLAY_NAME, title)
        put(DESCRIPTION, description)
        put(MIME_TYPE, "image/jpeg")
        put(DATE_ADDED, System.currentTimeMillis())
        put(DATE_TAKEN, System.currentTimeMillis())
    }
    var url: Uri? = null

    try {
        url = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        openOutputStream(url).use {
            source.compress(Bitmap.CompressFormat.JPEG, 50, it)
        }
        val id = ContentUris.parseId(url)
        val miniThumb = getThumbnail(this, id, MINI_KIND, null)
        storeThumbnail(miniThumb, id, 50f, 50f, MICRO_KIND)
        return true
    } catch (e: Exception) {
        if (url != null) {
            delete(url, null, null)
        }
        return false
    }
}

private fun ContentResolver.storeThumbnail(
        source: Bitmap,
        id: Long,
        width: Float,
        height: Float,
        kind: Int
): Bitmap? {
    val matrix = Matrix().apply {
        setScale(width / source.width, height / source.height)
    }
    val thumb = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
    )
    val values = ContentValues().apply {
        put(KIND, kind)
        put(IMAGE_ID, id.toInt())
        put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
        put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)
    }
    val url = insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)
    openOutputStream(url).use {
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return thumb
}

private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        requestWidth: Int,
        requestHeight: Int
): Int {
    val outHeight = options.outHeight
    val outWidth = options.outWidth
    return if (outHeight > requestHeight || outWidth > requestWidth) {
        val halfWidth = outWidth / 2
        val halfHeight = outHeight / 2
        generateSequence(1) { it * 2 }
                .takeWhile { halfHeight / it >= requestHeight && halfWidth / it >= requestWidth }
                .lastOrNull() ?: 1
    } else 1
}