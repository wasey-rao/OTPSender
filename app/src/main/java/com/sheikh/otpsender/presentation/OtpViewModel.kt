package com.sheikh.otpsender.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sheikh.otpsender.data.source.ContactDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val contactDataStore: ContactDataStore
) : ViewModel() {

    val contacts = contactDataStore.contactsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val forwardingEnabled = contactDataStore.forwardingEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val lastOtp = contactDataStore.lastOtpFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "No OTP received yet"
    )
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    val isSmsForwardingEnabled = contactDataStore.isSmsForwardingEnabledFlow
    val isServerForwardingEnabled = contactDataStore.isServerForwardingEnabledFlow

    fun toggleSmsForwarding(enabled: Boolean) {
        viewModelScope.launch { contactDataStore.setSmsForwardingEnabled(enabled) }
    }

    fun toggleServerForwarding(enabled: Boolean) {
        viewModelScope.launch { contactDataStore.setServerForwardingEnabled(enabled) }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun addContact(number: String) {
        viewModelScope.launch { contactDataStore.addContact(number) }
    }

    fun removeContact(number: String) {
        viewModelScope.launch { contactDataStore.removeContact(number) }
    }

    fun setForwardingEnabled(enabled: Boolean) {
        viewModelScope.launch { contactDataStore.setForwardingEnabled(enabled) }
    }
}