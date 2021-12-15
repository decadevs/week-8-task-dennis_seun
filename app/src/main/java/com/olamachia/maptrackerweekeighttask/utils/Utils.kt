package com.olamachia.maptrackerweekeighttask.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import android.graphics.Bitmap
import android.graphics.Canvas

import androidx.core.content.ContextCompat

import android.graphics.drawable.Drawable

import com.google.android.gms.maps.model.BitmapDescriptor


fun View.snackbar(message: String) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also { snackbar ->
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
    }.show()
}


//fun generateBitmapDescriptorFromRes(
//    context: Context?, resId: Int
//): BitmapDescriptor {
//    val drawable = ContextCompat.getDrawable(context!!, resId)
//    drawable!!.setBounds(
//        0,
//        0,
//        drawable.intrinsicWidth,
//        drawable.intrinsicHeight
//    )
//    val bitmap = Bitmap.createBitmap(
//        drawable.intrinsicWidth,
//        drawable.intrinsicHeight,
//        Bitmap.Config.ARGB_8888
//    )
//    val canvas = Canvas(bitmap)
//    drawable.draw(canvas)
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}