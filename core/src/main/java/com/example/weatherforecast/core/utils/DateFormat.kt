package com.example.weatherforecast.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(epochSec: Long): String =
    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(epochSec * 1000))

fun formatDateTime(epochSec: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochSec * 1000))