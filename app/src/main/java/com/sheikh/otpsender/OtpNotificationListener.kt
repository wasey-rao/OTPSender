package com.sheikh.otpsender

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.sheikh.otpsender.data.repository.OtpRepository
import com.sheikh.otpsender.data.source.ContactDataStore
import com.sheikh.otpsender.domain.models.SmsMessageData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OtpNotificationListener : NotificationListenerService() {

    @Inject lateinit var otpRepository: OtpRepository
    @Inject lateinit var contactDataStore: ContactDataStore

    private var lastTriggerTime = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 3000) return // skip if triggered too soon
        lastTriggerTime = now

        val packageName = sbn.packageName
        val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(applicationContext)

        if (packageName == defaultSmsApp) {
            CoroutineScope(Dispatchers.IO).launch {
                val lastProcessedDate = contactDataStore.lastProcessedDateFlow.first()
                val latestSms = getLatestIncomingSms(applicationContext, lastProcessedDate)
                latestSms?.let { smsMessageData ->
                    otpRepository.onPotentialOtpReceived(smsMessageData)
                }
            }
        }
    }

    private fun getLatestIncomingSms(context: Context, lastProcessedDate: Long): SmsMessageData? {
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            "${Telephony.Sms.DATE} > ?",
            arrayOf(lastProcessedDate.toString()),
            "${Telephony.Sms.DATE} DESC LIMIT 1"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val contactName = getContactNameFromNumber(context, address) ?: address
                return SmsMessageData(
                    sender = contactName,
                    body = body,
                    timestamp = date
                ).also {
                    CoroutineScope(Dispatchers.IO).launch {
                        contactDataStore.saveLastProcessedDate(date)
                    }
                }
            }
        }
        return null
    }

    /**
     * Helper to resolve contact name from number.
     */
    @SuppressLint("Range")
    private fun getContactNameFromNumber(context: Context, number: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null
    }
}