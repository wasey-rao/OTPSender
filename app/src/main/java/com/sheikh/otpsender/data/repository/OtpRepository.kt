package com.sheikh.otpsender.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OtpRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val LAST_OTP = stringPreferencesKey("last_otp")
        val CONTACTS = stringSetPreferencesKey("contacts")
    }

    val lastOtp: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_OTP] ?: ""
    }

    val contacts: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.CONTACTS] ?: emptySet()
    }

    suspend fun onOtpDetected(otp: String, source: String, message: String) {


        // Forward to selected contacts
        val contacts = dataStore.data.map { prefs ->
            prefs[stringSetPreferencesKey("contacts")] ?: emptySet()
        }.first()

    }

    suspend fun saveOtp(otp: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_OTP] = otp
        }
    }

    suspend fun addContact(contact: String) {
        dataStore.edit { prefs ->
            val set = prefs[Keys.CONTACTS]?.toMutableSet() ?: mutableSetOf()
            set.add(contact)
            prefs[Keys.CONTACTS] = set
        }
    }

    suspend fun removeContact(contact: String) {
        dataStore.edit { prefs ->
            val set = prefs[Keys.CONTACTS]?.toMutableSet() ?: mutableSetOf()
            set.remove(contact)
            prefs[Keys.CONTACTS] = set
        }
    }
}
