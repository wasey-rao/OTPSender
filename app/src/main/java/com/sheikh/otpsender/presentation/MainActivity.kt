package com.sheikh.otpsender.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.sheikh.otpsender.SmsBroadcastReceiver
import com.sheikh.otpsender.ui.theme.Charcoal
import com.sheikh.otpsender.ui.theme.OTPSenderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: OtpViewModel by viewModels()
    private val smsBroadcastReceiver = SmsBroadcastReceiver()

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsBroadcastReceiver, intentFilter)
        }

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OTPSenderTheme {
                OtpForwarderApp(viewModel)
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    val smsPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            onGranted()
        } else {
            Toast.makeText(context, "SMS permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(smsPermissions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpForwarderApp(viewModel: OtpViewModel? = null) {
    val contacts = viewModel?.contacts?.collectAsStateWithLifecycle()
    val lastOtp = viewModel?.lastOtp?.collectAsStateWithLifecycle()

    var newContact by remember { mutableStateOf("") }

    val context = LocalContext.current
    val smsPermissions = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(context, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "SMS permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(smsPermissions)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("OTP Forwarder") }) },
        containerColor = Charcoal
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel?.startListening() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start OTP Listener")
            }

            OutlinedTextField(
                value = newContact,
                onValueChange = { newContact = it },
                label = { Text("Add Contact Number") },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel?.addContact(newContact)
                        newContact = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Trusted Contacts:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(contacts!!.value.toList()) { number ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(number)
                        IconButton(onClick = { viewModel.removeContact(number) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Last OTP: ${lastOtp?.value}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OTPSenderTheme {
        OtpForwarderApp()
    }
}