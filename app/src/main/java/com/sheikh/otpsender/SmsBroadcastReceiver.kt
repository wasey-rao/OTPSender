package com.sheikh.otpsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.sheikh.otpsender.data.repository.OtpRepository
import com.sheikh.otpsender.presentation.OtpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: OtpRepository

    private var listener: SmsListener? = null

    fun setListener(listener: SmsListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            val extras = intent.extras
            val status = extras?.get(SMS_RETRIEVE_STATUS) as? Status
            Log.d("SmsBroadcastReceiver", "onReceive: $status")
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SMS_RETRIEVED_MESSAGE) as? String
                    message?.let { otp ->
                        Log.d("SmsBroadcastReceiver", "onReceive: $otp")
                        CoroutineScope(Dispatchers.IO).launch {
                        repository.saveOtp(otp)
                        repository.contacts.first().forEach { number ->
                            SmsManager.getDefault().sendTextMessage(number, null, "OTP: $otp", null, null)
                        }
                    } }
                }
                CommonStatusCodes.TIMEOUT -> {
                    listener?.onSmsTimeOut()
                    Log.d("SmsBroadcastReceiver", "onReceive: TIMEOUT")
                }
            }
        }
    }

    interface SmsListener {
        fun onSmsReceived(message: String)fun onSmsTimeOut()
    }

    companion object {
        private const val SMS_RETRIEVED_ACTION = "com.google.android.gms.auth.api.phone.SMS_RETRIEVED"
        private const val SMS_RETRIEVE_STATUS = "com.google.android.gms.auth.api.phone.EXTRA_STATUS"
        private const val SMS_RETRIEVED_MESSAGE = "com.google.android.gms.auth.api.phone.EXTRA_SMS_MESSAGE"
    }
}