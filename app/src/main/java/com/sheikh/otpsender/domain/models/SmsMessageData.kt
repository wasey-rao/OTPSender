package com.sheikh.otpsender.domain.models

data class SmsMessageData(
    val sender: String,
    val body: String,
    val timestamp: Long
)