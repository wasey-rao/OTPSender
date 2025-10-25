package com.sheikh.otpsender.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.sheikh.otpsender.data.repository.OtpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val app: Application,
    private val repository: OtpRepository
) : ViewModel() {

    private val smsRetrieverClient = SmsRetriever.getClient(app)

    fun startListening() {
        smsRetrieverClient.startSmsRetriever()
            .addOnSuccessListener { Log.d("OTP", "SMS Retriever started") }
            .addOnFailureListener { Log.e("OTP", "Failed to start SMS Retriever", it) }
    }

    val contacts = repository.contacts.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptySet()
    )

    val lastOtp = repository.lastOtp.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ""
    )

    fun addContact(number: String) = viewModelScope.launch {
        repository.addContact(number)
    }

    fun removeContact(number: String) = viewModelScope.launch {
        repository.removeContact(number)
    }
}