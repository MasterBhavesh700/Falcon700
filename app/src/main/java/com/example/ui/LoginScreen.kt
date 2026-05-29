package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LoginScreen(viewModel: FalconViewModel) {
    val ElectricGold = com.example.ui.theme.ElectricGold
    val MedicalCyan = com.example.ui.theme.MedicalCyan
    val authError by viewModel.authError.collectAsState()

    var isRegisterTab by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicSpaceBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, ElectricGold, RoundedCornerShape(20.dp))
                    .background(CosmicSlateCard)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦅",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FALCON COMMAND",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 24.sp,
                letterSpacing = 2.sp
            )

            Text(
                text = "Secure Active Recall & Ledger Vault",
                color = MutedSlate,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Auth Tab Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Sign-In vs Register toggle tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicSpaceBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isRegisterTab = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_signin"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isRegisterTab) CosmicSlateCard else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(
                                "SIGN IN",
                                color = if (!isRegisterTab) ElectricGold else SteelGrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { isRegisterTab = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_register"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegisterTab) CosmicSlateCard else Color.Transparent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(
                                "REGISTER",
                                color = if (isRegisterTab) ElectricGold else SteelGrayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (authError.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF5C2626))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⚠️ $authError",
                                color = Color(0xFFFF8A8A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (successMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E4620))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✅ $successMessage",
                                color = Color(0xFFA5FFAA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // For Registration Display Name and Rank Input fields
                    if (isRegisterTab) {
                        Text(
                            text = "CADET FULL NAME:",
                            fontWeight = FontWeight.Bold,
                            color = ElectricGold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. Bhavesh Patel", color = MutedSlate.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SteelGrayText,
                                unfocusedTextColor = SteelGrayText,
                                focusedBorderColor = ElectricGold,
                                unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                                focusedContainerColor = CosmicSpaceBg,
                                unfocusedContainerColor = CosmicSpaceBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CADET CORPS RANK / CLASS:",
                            fontWeight = FontWeight.Bold,
                            color = ElectricGold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rank,
                            onValueChange = { rank = it },
                            placeholder = { Text("e.g. MBBS 3rd Year (GMC Ambikapur)", color = MutedSlate.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = MutedSlate) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SteelGrayText,
                                unfocusedTextColor = SteelGrayText,
                                focusedBorderColor = ElectricGold,
                                unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                                focusedContainerColor = CosmicSpaceBg,
                                unfocusedContainerColor = CosmicSpaceBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_rank"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    Text(
                        text = "EMAIL ID ADDRESS:",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("e.g. cadet@gmc.edu", color = MutedSlate.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MutedSlate) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                            focusedContainerColor = CosmicSpaceBg,
                            unfocusedContainerColor = CosmicSpaceBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password/Passcode Field
                    Text(
                        text = "SECURE COMMAND PASSCODE:",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Enter account digits/text", color = MutedSlate.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MutedSlate) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                            focusedContainerColor = CosmicSpaceBg,
                            unfocusedContainerColor = CosmicSpaceBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Button Trigger
                    Button(
                        onClick = {
                            if (isRegisterTab) {
                                viewModel.registerNewUser(email, password, name, rank) { success ->
                                    if (success) {
                                        successMessage = "New database ledger space created!"
                                    }
                                }
                            } else {
                                viewModel.signInUser(email, password) { success ->
                                    if (success) {
                                        successMessage = "Handshake verified. Launching deck."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_auth_submit")
                    ) {
                        Text(
                            text = if (isRegisterTab) "INITIALIZE NEW SECURE SPACE" else "LAUNCH COMMISSION DECK ⚔️",
                            fontWeight = FontWeight.ExtraBold,
                            color = CosmicSpaceBg,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fast Track Demo Mode button
                    Button(
                        onClick = {
                            viewModel.signInUser("bp111223@gmail.com", "falcon700") { success ->
                                if (success) {
                                    successMessage = "Default Cadet authorized!"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSpaceBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MedicalCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .height(44.dp)
                            .testTag("btn_quick_demo")
                    ) {
                        Text(
                            text = "QUICK STUDY ACCESS (DEMO CADET) 🎓",
                            color = MedicalCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Informational Security note
            Text(
                text = "🛡️ Secure on-device AES-standard hashing database active. Handshake requests are preserved with strict end-to-end sandbox privacy.",
                color = MutedSlate,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}
