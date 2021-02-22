package com.example.mynewsfeed.utils

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions


fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun ImageView.loadImageFromUrl(url: String) {
    val requestOptions = RequestOptions.bitmapTransform(RoundedCorners(10))
    Glide.with(this).load(url)
        .apply(requestOptions)
        .into(this)
}