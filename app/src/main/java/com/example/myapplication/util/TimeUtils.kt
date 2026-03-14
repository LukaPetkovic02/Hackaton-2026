package com.example.myapplication.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun currentTimestamp(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    return formatter.format(Date())
}
