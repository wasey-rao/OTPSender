package com.sheikh.otpsender.presentation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sheikh.otpsender.ui.theme.Charcoal
import com.sheikh.otpsender.ui.theme.OTPSenderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpForwarderApp(viewModel: MainViewModel? = null ) {
    val contacts = viewModel?.contacts?.collectAsState()
    val forwardingEnabled = viewModel?.forwardingEnabled?.collectAsState()
    val lastOtp by viewModel?.lastOtp?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("No OTP received yet") }
    val smsEnabled = viewModel?.isSmsForwardingEnabled?.collectAsStateWithLifecycle(false)
    val serverEnabled = viewModel?.isServerForwardingEnabled?.collectAsStateWithLifecycle(false)
    var newNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        viewModel?.updatePermissionStatus(allGranted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    val permissionGranted = viewModel?.permissionGranted?.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Charcoal
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "OTP Forwarder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF146C43),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top
            ) {
                Text("OTP Forwarder Settings", style = MaterialTheme.typography.titleLarge)

                // 🔄 SMS Forward Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Forward OTP via SMS")
                    Switch(
                        checked = smsEnabled?.value == true,
                        onCheckedChange = { viewModel?.toggleSmsForwarding(it) }
                    )
                }

                // 🌐 Server Forward Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Send OTP to Server")
                    Switch(
                        checked = serverEnabled?.value == true,
                        onCheckedChange = { viewModel?.toggleServerForwarding(it) }
                    )
                }

                HorizontalDivider(
                    Modifier.padding(vertical = 8.dp),
                    DividerDefaults.Thickness,
                    DividerDefaults.color
                )
                Spacer(Modifier.height(12.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF146C43)),
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Enable Notification Access", color = Color.White)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    if (permissionGranted?.value == true) "Permissions granted ✅"
                    else "Grant SMS permissions to start",
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Forwarding:", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = forwardingEnabled?.value == true,
                        onCheckedChange = { viewModel?.setForwardingEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Green)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text("Forwarding Contacts:", color = Color.White, fontSize = 16.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(contacts?.value?.toList()?: emptyList()) { contact ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(contact.toString(), color = Color.White)
                            IconButton(onClick = { viewModel?.removeContact(contact.toString()) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = newNumber,
                    onValueChange = { newNumber = it },
                    label = { Text("Add contact number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (newNumber.isNotBlank()) {
                            viewModel?.addContact(newNumber)
                            newNumber = ""
                        } else {
                            Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF146C43))
                ) {
                    Text("Add", color = Color.White)
                }
                Spacer(Modifier.height(20.dp))

                // 🟩 Show Last OTP at the bottom
                Text(
                    text = "Last OTP Received:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = lastOtp,
                    color = Color(0xFFB8E994),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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