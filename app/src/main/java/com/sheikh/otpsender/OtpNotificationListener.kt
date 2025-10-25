package com.sheikh.otpsender

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.util.Log
import com.sheikh.otpsender.data.repository.OtpRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OtpNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var otpRepository: OtpRepository  // or use your own class to forward SMS

    private val otpRegex = Regex("\\b\\d{4,8}\\b") // 4–8 digit OTPs

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("OtpService", "onNotificationPosted: ")
        val notification = sbn.notification ?: return
        val extras = notification.extras
        val smsPackageName = Telephony.Sms.getDefaultSmsPackage(this )
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

        if (!text.isNullOrEmpty() && isLikelyOtpNotification(title, text)) {
            val otp = otpRegex.find(text)?.value
            if (otp != null) {
                Log.d("OtpService", "OTP found: $otp from $title")

                CoroutineScope(Dispatchers.IO).launch {
                    // Save to log & forward
                    otpRepository.saveOtp(otp)
                    otpRepository.contacts.first().forEach { number ->
                        SmsManager.getDefault().sendTextMessage(number, null, "OTP: $otp", null, null)
                    }
                }
            }
        }
    }

    private fun isLikelyOtpNotification(title: String?, message: String): Boolean {
        val keywords = listOf("OTP", "code", "verification", "password")
        return keywords.any { message.contains(it, ignoreCase = true) || title?.contains(it, true) == true }
    }
}