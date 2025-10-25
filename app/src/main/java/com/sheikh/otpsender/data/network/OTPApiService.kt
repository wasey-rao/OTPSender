package com.sheikh.otpsender.data.network

import com.sheikh.otpsender.data.network.dto.OtpPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OtpApiService {
    @POST("otp")
    suspend fun sendOtp(@Body otpPayload: OtpPayload): Response<Unit>
}