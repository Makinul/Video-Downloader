package com.makinul.instragram.video.downloader.utils

import android.util.Log

object AppConstant {


    const val TAG = "AppConstant"

    fun showLog(message: String = "Test Data", tag: String = TAG) {
        Log.d(tag, message)
    }
}