package com.sheikh.otpsender.data.network.dto

data class OtpPayload(
    val otp: String,
    val sender: String,
    val timestamp: String
)