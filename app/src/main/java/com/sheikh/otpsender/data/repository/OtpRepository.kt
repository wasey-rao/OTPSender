package com.sheikh.otpsender.data.repository

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.sheikh.otpsender.data.network.OtpApiService
import com.sheikh.otpsender.data.network.dto.OtpPayload
import com.sheikh.otpsender.data.source.ContactDataStore
import com.sheikh.otpsender.domain.models.SmsMessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpRepository @Inject constructor(
    private val contactDataStore: ContactDataStore,
    private val otpApiService: OtpApiService
) {
    private val otpRegex = Regex("\\b\\d{4,8}\\b")

    suspend fun onPotentialOtpReceived(smsMessageData: SmsMessageData) {
        val forwardingEnabled = contactDataStore.forwardingEnabledFlow.first()
        val isSmsForwardingEnabled = contactDataStore.isSmsForwardingEnabledFlow.first()
        val isServerForwardingEnabled = contactDataStore.isServerForwardingEnabledFlow.first()
        if (!forwardingEnabled) return

        val otp = otpRegex.find(smsMessageData.body)?.value ?: return
        Log.d("OtpRepository", "Detected OTP: $otp from ${smsMessageData.sender}")

        // Save last OTP
        contactDataStore.saveLastOtp(otp)

        if (isSmsForwardingEnabled) {
            val contacts = contactDataStore.contactsFlow.first()
            sendOtpToContacts(otp, contacts)
        }

        if (isServerForwardingEnabled) {
            sendOtpToServer(smsMessageData.sender, otp)
        }
    }

    private fun sendOtpToContacts(otp: String, contacts: Set<String>) {
        val smsManager = SmsManager.getDefault()
        contacts.forEach { contact ->
            smsManager.sendTextMessage(contact, null, "Forwarded OTP: $otp", null, null)
        }
    }

    private suspend fun sendOtpToServer(sender: String, otp: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = otpApiService.sendOtp(
                    OtpPayload(
                        otp = otp,
                        sender = sender,
                        timestamp = Date().time.toString()
                    )
                )
                if (response.isSuccessful) {
                    Log.d("OtpRepository", "OTP sent to server successfully")
                } else {
                    Log.e("OtpRepository", "Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("OtpRepository", "Error sending OTP to server: ${e.message}")
            }
        }
    }
}
