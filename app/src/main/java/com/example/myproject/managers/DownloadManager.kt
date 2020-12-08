package com.example.myproject.managers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

fun downloadImageFromNetwork(source: String): Bitmap {
    val url = URL(source)
    return BitmapFactory.decodeStream(url.openConnection().getInputStream())
}