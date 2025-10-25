package com.sheikh.otpsender.data.source

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "otp_prefs")

class ContactDataStore(
    private val context: Context
) {

    companion object {
        val CONTACTS = stringSetPreferencesKey("forward_contacts")
        val FORWARDING_ENABLED = booleanPreferencesKey("forwarding_enabled")
        val LAST_OTP = stringPreferencesKey("last_otp")
        val LAST_PROCESSED_SMS_DATE = longPreferencesKey("last_sms_date")
        val SMS_FORWARD = booleanPreferencesKey("sms_forward_enabled")
        val SERVER_FORWARD = booleanPreferencesKey("server_forward_enabled")

    }

    val contactsFlow: Flow<Set<String>> = context.dataStore.data
        .map { it[CONTACTS] ?: emptySet() }

    val forwardingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { it[FORWARDING_ENABLED] ?: true }

    val isSmsForwardingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { it[SMS_FORWARD] ?: false }
    val isServerForwardingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { it[SERVER_FORWARD] ?: false }

    suspend fun addContact(number: String) {
        context.dataStore.edit { prefs ->
            val updated = prefs[CONTACTS]?.toMutableSet() ?: mutableSetOf()
            updated.add(number)
            prefs[CONTACTS] = updated
        }
    }

    suspend fun removeContact(number: String) {
        context.dataStore.edit { prefs ->
            val updated = prefs[CONTACTS]?.toMutableSet() ?: mutableSetOf()
            updated.remove(number)
            prefs[CONTACTS] = updated
        }
    }

    suspend fun setForwardingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[FORWARDING_ENABLED] = enabled
        }
    }

    val lastOtpFlow: Flow<String> = context.dataStore.data
        .map { it[LAST_OTP] ?: "No OTP received yet" }

    suspend fun saveLastOtp(otp: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_OTP] = otp
        }
    }

    suspend fun saveLastProcessedDate(date: Long) {
        context.dataStore.edit { prefs -> prefs[LAST_PROCESSED_SMS_DATE] = date }
    }

    val lastProcessedDateFlow: Flow<Long> = context.dataStore.data
        .map { it[LAST_PROCESSED_SMS_DATE] ?: 0L }

    suspend fun setSmsForwardingEnabled(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[SMS_FORWARD] = value }
    }

    suspend fun setServerForwardingEnabled(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[SERVER_FORWARD] = value }
    }
}