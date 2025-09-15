package com.sheikh.otpsender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsBroadcastReceiver : BroadcastReceiver() {

    private var listener: SmsListener? = null

    fun setListener(listener: SmsListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == SMS_RETRIEVED_ACTION) {
            val extras = intent.extras
            val status = extras?.get(SMS_RETRIEVE_STATUS) as? Status
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SMS_RETRIEVED_MESSAGE) as? String
                    message?.let { listener?.onSmsReceived(it) }
                }
                CommonStatusCodes.TIMEOUT -> {
                    listener?.onSmsTimeOut()
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