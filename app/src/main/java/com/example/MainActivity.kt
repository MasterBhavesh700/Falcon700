package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ui.BookIntel
import com.example.ui.BookLibrary
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DailyLogEntity
import com.example.data.FlashcardEntity
import com.example.data.HabitEntity
import com.example.ui.FalconViewModel
import com.example.ui.LoginScreen
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FalconViewModel = viewModel()
            val currentTheme by viewModel.appTheme.collectAsState()
            MyApplicationTheme(appTheme = currentTheme) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: FalconViewModel = viewModel()) {
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    val currentTab by viewModel.currentTab.collectAsState()
    val currentLog by viewModel.currentLog.collectAsState()
    val allDailyLogs by viewModel.allDailyLogs.collectAsState()
    val allFlashcards by viewModel.allFlashcards.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicSpaceBg)
                .statusBarsPadding()
                .navigationBarsPadding(),
            bottomBar = {
                NavigationBar(
                    containerColor = CosmicSlateCard,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Command") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricGold,
                            selectedTextColor = ElectricGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                            indicatorColor = CosmicSpaceBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Daily Habits") },
                        label = { Text("Discipline") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricGold,
                            selectedTextColor = ElectricGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                            indicatorColor = CosmicSpaceBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Active Recall") },
                        label = { Text("Recall Arena") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricGold,
                            selectedTextColor = ElectricGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                            indicatorColor = CosmicSpaceBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Honor Log") },
                        label = { Text("Honor Roll") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricGold,
                            selectedTextColor = ElectricGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                            indicatorColor = CosmicSpaceBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Wealth") },
                        label = { Text("TrueYield") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricGold,
                            selectedTextColor = ElectricGold,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                            indicatorColor = CosmicSpaceBg
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CosmicSpaceBg)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> DashboardTab(viewModel, currentLog)
                        1 -> DisciplineTab(viewModel)
                        2 -> ActiveRecallTab(viewModel, allFlashcards)
                        3 -> HonorRollTab(allDailyLogs, viewModel)
                        4 -> TrueYieldTab(viewModel, currentLog)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: DASHBOARD (THE COMMAND CENTRE)
// ==========================================
@Composable
fun DashboardTab(viewModel: FalconViewModel, currentLog: DailyLogEntity?) {
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    val selectedTheme by viewModel.appTheme.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Core Mnemonic / Greeting Banner
        item {
            val currentUser by viewModel.currentUser.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("greeting_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Prestige", tint = ElectricGold)
                            Text(
                                text = currentUser?.name?.uppercase() ?: "GUEST CADET",
                                fontWeight = FontWeight.Bold,
                                color = ElectricGold,
                                fontSize = 16.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.signOutUser() },
                            modifier = Modifier.size(28.dp).testTag("btn_logout")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentUser?.rank ?: "MBBS Cadet",
                        fontSize = 11.sp,
                        color = MedicalCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"A Falcon does not lie to himself, does not flinch from the hard right, and does not postpone what fear disguises as planning.\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = SteelGrayText,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Cloud Telemetry Sync Panel
        item {
            val currentUser by viewModel.currentUser.collectAsState()
            val syncing by viewModel.syncing.collectAsState()
            val syncMessage by viewModel.syncMessage.collectAsState()
            val allDailyLogs by viewModel.allDailyLogs.collectAsState()
            val allFlashcards by viewModel.allFlashcards.collectAsState()

            var showSettings by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalCyan.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Cloud", tint = MedicalCyan, modifier = Modifier.size(20.dp))
                        Text(
                            text = "CLOUD PERSISTENCE HANDSHAKE",
                            fontWeight = FontWeight.ExtraBold,
                            color = MedicalCyan,
                            fontSize = 12.sp,
                            letterSpacing = 1.0.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configured endpoint: ${currentUser?.cloudEndpoint ?: "SQLite Sandbox Mode"}",
                        color = SteelGrayText,
                        fontSize = 11.sp
                    )
                    
                    if (currentUser?.lastSyncTime != null && currentUser?.lastSyncTime != 0L) {
                        val simpleSdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        Text(
                            text = "Last Handshake: ${simpleSdf.format(java.util.Date(currentUser?.lastSyncTime ?: 0L))}",
                            color = ElectricGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Status: Local offline cache active (Unsynchronized)",
                            color = MutedSlate,
                            fontSize = 11.sp
                        )
                    }

                    if (syncMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSpaceBg)
                                .padding(10.dp)
                                .border(1.dp, MedicalCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "📡 $syncMessage",
                                color = MedicalCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        enabled = !syncing,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_trigger_sync")
                    ) {
                        if (syncing) {
                            CircularProgressIndicator(color = CosmicSpaceBg, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SYNCHRONIZING...", color = CosmicSpaceBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("FORCE SYNCHRONIZE TELEMETRY LEDGER ⚡", color = CosmicSpaceBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showSettings = !showSettings },
                            modifier = Modifier.testTag("btn_toggle_cloud_settings")
                        ) {
                            Text(
                                text = if (showSettings) "HIDE SETTINGS ▲" else "ENDPOINT CONFIGURATION ▼",
                                color = ElectricGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showSettings) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MedicalCyan.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        var tempEndpoint by remember { mutableStateOf(currentUser?.cloudEndpoint ?: "") }
                        var tempEnabled by remember { mutableStateOf(currentUser?.cloudEnabled ?: false) }

                        Text(
                            text = "CUSTOM SECURED REMOTE URL ENDPOINT:",
                            fontWeight = FontWeight.Bold,
                            color = ElectricGold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = tempEndpoint,
                            onValueChange = { tempEndpoint = it },
                            placeholder = { Text("https://ais-dev-tc55jkxijcuobmn47qf4mz.cloud/telemetry", color = MutedSlate.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SteelGrayText,
                                unfocusedTextColor = SteelGrayText,
                                focusedBorderColor = ElectricGold,
                                unfocusedBorderColor = MutedSlate.copy(alpha = 0.4f),
                                focusedContainerColor = CosmicSpaceBg,
                                unfocusedContainerColor = CosmicSpaceBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("cloud_endpoint_edit_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SECURE CLOUD PORTAL SWITCH",
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricGold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Enable automated background telemetry snapshots if endpoint responds.",
                                    color = MutedSlate,
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = tempEnabled,
                                onCheckedChange = { tempEnabled = it },
                                modifier = Modifier.testTag("cloud_sync_toggle_config")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Schema Database Statistics Panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSpaceBg)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "LOCAL SQLITE TELEMETRY ANALYSIS",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MedicalCyan,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text("• Active Profile Cadet: ${currentUser?.email}", color = SteelGrayText, fontSize = 10.sp)
                                Text("• Total Schema Flashcards: ${allFlashcards.size}", color = SteelGrayText, fontSize = 10.sp)
                                Text("• History Record Sessions: ${allDailyLogs.size}", color = SteelGrayText, fontSize = 10.sp)
                                Text("• Encryption Protocol: Sandbox AES-256", color = MasteredGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { 
                                viewModel.updateUserConfiguration(tempEndpoint, tempEnabled)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("btn_save_cloud_configuration")
                        ) {
                            Text("SAVE CONFIGURATION PARAMETERS 💾", color = CosmicSpaceBg, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Countdown card (60 days out from modern medical examinations)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "3RD YEAR MBBS EXAMINATIONS",
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "60",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricGold
                            )
                            Text(
                                text = "Days Remaining",
                                fontSize = 11.sp,
                                color = SteelGrayText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(56.dp)
                                .width(1.dp),
                            color = MutedSlate.copy(alpha = 0.3f)
                        )
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "• Forensic Medicine & Tox",
                                fontSize = 12.sp,
                                color = MedicalCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Community Med (PSM)",
                                fontSize = 12.sp,
                                color = MedicalCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Source: Reddy FMT & K Park",
                                fontSize = 10.sp,
                                color = MutedSlate,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }

        // Falcon Code Habits Checklist (Mandatory Pillars)
        item {
            Text(
                text = "THE FALCON CADET CODE",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Pillar 1: Body
                    HabitRow(
                        pillarName = "BODY",
                        desc = "Trained + ate real, healthy food (No Garbage)",
                        isChecked = currentLog?.bodyChecked ?: false,
                        color = MasteredGreen,
                        tag = "pillar_body"
                    ) { checked ->
                        viewModel.updateDailyChecked(body = checked)
                    }
                    Divider(color = MutedSlate.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    // Pillar 2: Mind
                    HabitRow(
                        pillarName = "MIND",
                        desc = "One deep study block completed BEFORE building",
                        isChecked = currentLog?.mindChecked ?: false,
                        color = MedicalCyan,
                        tag = "pillar_mind"
                    ) { checked ->
                        viewModel.updateDailyChecked(mind = checked)
                    }
                    Divider(color = MutedSlate.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    // Pillar 3: Honor
                    HabitRow(
                        pillarName = "HONOR",
                        desc = "Chose the harder right & wrote today's diary",
                        isChecked = currentLog?.honorChecked ?: false,
                        color = HoldAmber,
                        tag = "pillar_honor"
                    ) { checked ->
                        viewModel.updateDailyChecked(honor = checked)
                    }
                    Divider(color = MutedSlate.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    // Pillar 4: Hold
                    HabitRow(
                        pillarName = "HOLD",
                        desc = "No avoidance or procrastination in productivity disguise",
                        isChecked = currentLog?.holdChecked ?: false,
                        color = FlinchRed,
                        tag = "pillar_hold"
                    ) { checked ->
                        viewModel.updateDailyChecked(hold = checked)
                    }
                }
            }
        }

        // Study Hours Logging
        item {
            Text(
                text = "STUDY HOUR LOGS (TODAY)",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Forensic Medicine (FMT):",
                            color = SteelGrayText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val current = currentLog?.studyHoursFmt ?: 0f
                                if (current >= 0.5f) viewModel.updateDailyChecked(hrsFmt = current - 0.5f)
                            }) {
                                Text("-", fontWeight = FontWeight.Bold, color = ElectricGold, fontSize = 20.sp)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f hrs", currentLog?.studyHoursFmt ?: 0f),
                                color = ElectricGold,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                val current = currentLog?.studyHoursFmt ?: 0f
                                viewModel.updateDailyChecked(hrsFmt = current + 0.5f)
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "More", tint = ElectricGold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Community Medicine (PSM):",
                            color = SteelGrayText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val current = currentLog?.studyHoursPsm ?: 0f
                                if (current >= 0.5f) viewModel.updateDailyChecked(hrsPsm = current - 0.5f)
                            }) {
                                Text("-", fontWeight = FontWeight.Bold, color = ElectricGold, fontSize = 20.sp)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f hrs", currentLog?.studyHoursPsm ?: 0f),
                                color = ElectricGold,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                val current = currentLog?.studyHoursPsm ?: 0f
                                viewModel.updateDailyChecked(hrsPsm = current + 0.5f)
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "More", tint = ElectricGold)
                            }
                        }
                    }
                }
            }
        }

        // Today's Diary Text Field
        item {
            Text(
                text = "HONOR CODE RECORD (DIARY)",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var diaryText by remember { mutableStateOf("") }
                    LaunchedEffect(currentLog) {
                        if (currentLog != null && diaryText.isEmpty()) {
                            diaryText = currentLog.diaryLine
                        }
                    }

                    OutlinedTextField(
                        value = diaryText,
                        onValueChange = { diaryText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diary_input"),
                        label = { Text("Write your one honest daily line...", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f),
                            focusedLabelColor = ElectricGold,
                            cursorColor = ElectricGold,
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateDailyChecked(diary = diaryText, honor = true)
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_diary_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGold)
                    ) {
                        Text("Log Diary Line", color = CosmicSpaceBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Theme Customizer Section
        item {
            Text(
                text = "COMMAND VISUAL INTERFACE THEME",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 10.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("theme_card_deck"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customize the interface style to match Bhavesh's medical or financial mindset context.",
                        color = SteelGrayText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeValue.values().forEach { themeVal ->
                            val isSelected = selectedTheme == themeVal
                            val themeName = when (themeVal) {
                                AppThemeValue.GOLD -> "Solaris"
                                AppThemeValue.CYAN -> "Clinical"
                                AppThemeValue.EMERALD -> "Emerald"
                                AppThemeValue.MONOCHROME -> "Sleek"
                                AppThemeValue.CRIMSON -> "Crimson"
                            }
                            val swatchColor = themeVal.getPrimary()

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ElectricGold.copy(alpha = 0.15f) else CosmicSpaceBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ElectricGold else MutedSlate.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setAppTheme(themeVal) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(swatchColor)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = themeName,
                                        color = if (isSelected) ElectricGold else SteelGrayText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitRow(
    pillarName: String,
    desc: String,
    isChecked: Boolean,
    color: Color,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pillarName,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = SteelGrayText,
                fontSize = 12.sp
            )
        }
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = MutedSlate.copy(alpha = 0.6f)
            )
        )
    }
}


// ==========================================
// TAB 2: ACTIVE RECALL STUDY ARENA (AI BUDDY)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveRecallTab(viewModel: FalconViewModel, flashcards: List<FlashcardEntity>) {
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    val selectedEngine by viewModel.aiEngine.collectAsState()
    val selectedVoice by viewModel.aiVoiceStyle.collectAsState()

    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var currentCardIndex by remember { mutableStateOf(0) }
    var isCardFlipped by remember { mutableStateOf(false) }

    var aiQuery by remember { mutableStateOf("") }
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var selectedBookFilter by remember { mutableStateOf("ALL") }

    // Filter cards
    val filteredCards = remember(flashcards, selectedSubjectFilter) {
        if (selectedSubjectFilter == "ALL") flashcards
        else flashcards.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    // Reset card index if filter changes
    LaunchedEffect(selectedSubjectFilter) {
        currentCardIndex = 0
        isCardFlipped = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Study Subject Filters
        item {
            Text(
                text = "ACTIVE RECALL SANDBOX",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL", "FMT", "PSM", "FINANCE")
                filters.forEach { filter ->
                    val isSelected = selectedSubjectFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ElectricGold else CosmicSlateCard)
                            .clickable { selectedSubjectFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (filter == "ALL") "All Arena" else if (filter == "FMT") "FMT (Reddy)" else if (filter == "PSM") "PSM (K Park)" else "Finance (FIRE)",
                            color = if (isSelected) CosmicSpaceBg else SteelGrayText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Active Recall Flashcard Box
        if (filteredCards.isNotEmpty()) {
            val card = filteredCards.getOrNull(currentCardIndex)
            if (card != null) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Progress info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Card ${currentCardIndex + 1} of ${filteredCards.size} [${card.subject}]",
                                color = MutedSlate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // Mastery indicator
                            val stateColor = when (card.masteryState) {
                                "MASTERED" -> MasteredGreen
                                "HOLD" -> HoldAmber
                                "FLINCHED" -> FlinchRed
                                else -> MutedSlate
                            }
                            Text(
                                text = card.masteryState,
                                color = stateColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Flip card body
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .testTag("flashcard_body")
                                .clickable { isCardFlipped = !isCardFlipped },
                            colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!isCardFlipped) {
                                    // Question Side
                                    Text(
                                        text = "Q: " + card.question,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ElectricGold,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "TAP CARD TO REVEAL KEY ANSWER",
                                        color = MutedSlate,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                } else {
                                    // Answer Side
                                    Text(
                                        text = "A: " + card.answer,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SteelGrayText,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (card.explanation.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CosmicSpaceBg)
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = card.explanation,
                                                fontSize = 11.sp,
                                                color = MedicalCyan,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Recall confidence ratings (Only show when flipped)
                        AnimatedVisibility(
                            visible = isCardFlipped,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = "RATE YOUR RECALL:",
                                    fontWeight = FontWeight.Bold,
                                    color = MutedSlate,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Flinched
                                    Button(
                                        onClick = {
                                            viewModel.updateFlashcardMastery(card.id, "FLINCHED")
                                            isCardFlipped = false
                                            if (currentCardIndex < filteredCards.size - 1) {
                                                currentCardIndex++
                                            } else {
                                                currentCardIndex = 0
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_flinch"),
                                        colors = ButtonDefaults.buttonColors(containerColor = FlinchRed)
                                    ) {
                                        Text("Flinched ❌", color = CosmicSpaceBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    // Hold
                                    Button(
                                        onClick = {
                                            viewModel.updateFlashcardMastery(card.id, "HOLD")
                                            isCardFlipped = false
                                            if (currentCardIndex < filteredCards.size - 1) {
                                                currentCardIndex++
                                            } else {
                                                currentCardIndex = 0
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_hold"),
                                        colors = ButtonDefaults.buttonColors(containerColor = HoldAmber)
                                    ) {
                                        Text("Retained ⚠️", color = CosmicSpaceBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    // Mastered
                                    Button(
                                        onClick = {
                                            viewModel.updateFlashcardMastery(card.id, "MASTERED")
                                            isCardFlipped = false
                                            if (currentCardIndex < filteredCards.size - 1) {
                                                currentCardIndex++
                                            } else {
                                                currentCardIndex = 0
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_master"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MasteredGreen)
                                    ) {
                                        Text("Mastered ", color = CosmicSpaceBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Navigation Buttons
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                if (currentCardIndex > 0) {
                                    currentCardIndex--
                                } else {
                                    currentCardIndex = filteredCards.size - 1
                                }
                                isCardFlipped = false
                            }) {
                                Text("← PREV CARD", color = ElectricGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            TextButton(onClick = {
                                if (currentCardIndex < filteredCards.size - 1) {
                                    currentCardIndex++
                                } else {
                                    currentCardIndex = 0
                                }
                                isCardFlipped = false
                            }) {
                                Text("NEXT CARD →", color = ElectricGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No flashcards found for filter: $selectedSubjectFilter", color = MutedSlate)
                    }
                }
            }
        }

        // Add custom flashcard
        item {
            Text(
                text = "ADD CUSTOM FLASHCARD",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            var addSubject by remember { mutableStateOf("FMT") }
            var addQuestion by remember { mutableStateOf("") }
            var addAnswer by remember { mutableStateOf("") }
            var addExp by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("FMT", "PSM", "FINANCE").forEach { subj ->
                            val active = addSubject == subj
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (active) ElectricGold else CosmicSpaceBg)
                                    .clickable { addSubject = subj }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(subj, color = if (active) CosmicSpaceBg else SteelGrayText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = addQuestion,
                        onValueChange = { addQuestion = it },
                        label = { Text("Enter Question", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_card_q")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = addAnswer,
                        onValueChange = { addAnswer = it },
                        label = { Text("Enter Key Answer", color = MutedSlate) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_card_a")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = addExp,
                        onValueChange = { addExp = it },
                        label = { Text("Reference / Citation / Source (Optional)", color = MutedSlate) },
                        placeholder = { Text("e.g. Reddy FMT 34th Ed", color = MutedSlate.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (addQuestion.isNotEmpty() && addAnswer.isNotEmpty()) {
                                viewModel.addNewFlashcard(addSubject, addQuestion, addAnswer, addExp)
                                addQuestion = ""
                                addAnswer = ""
                                addExp = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("btn_save_custom_card")
                    ) {
                        Text("Save Card", color = CosmicSpaceBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ==========================================
        // BIBLIOPHILE PORTAL: LEADERSHIP & COACHING WISDOM
        // ==========================================
        item {
            val bookColorMap = mapOf(
                "HABITS" to Color(0xFFF97316),
                "STUDY" to Color(0xFF3B82F6),
                "PSYCHOLOGY" to Color(0xFF9C27B0),
                "PERFORMANCE" to Color(0xFFE53935),
                "DISCIPLINE" to Color(0xFFFF5252),
                "FINANCE" to Color(0xFF4CAF50),
                "PRODUCTIVITY" to Color(0xFF00BCD4)
            )

            val filteredBooksByCat = remember(selectedBookFilter) {
                if (selectedBookFilter == "ALL") BookLibrary.books
                else BookLibrary.books.filter { it.category.equals(selectedBookFilter, ignoreCase = true) }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(
                    text = "BIBLIOPHILE COACHING DECK 📚",
                    fontWeight = FontWeight.Bold,
                    color = ElectricGold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Access synthesized mental models and tactics from 14 elite world-class books. Tap any book to reveal its high-yield takeaway and trigger dedicated AI analyses.",
                    color = MutedSlate,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable category filters
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    val filterPills = listOf("ALL", "STUDY", "DISCIPLINE", "HABITS", "FINANCE", "PRODUCTIVITY", "PSYCHOLOGY")
                    items(filterPills) { filter ->
                        val active = selectedBookFilter == filter
                        val pillColor = bookColorMap[filter] ?: ElectricGold
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) pillColor.copy(alpha = 0.2f) else CosmicSlateCard)
                                .border(1.dp, if (active) pillColor else MutedSlate.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { selectedBookFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (active) pillColor else SteelGrayText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Flat horizontally scrollable book list
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredBooksByCat) { book ->
                        val isSelected = selectedBookId == book.id
                        val bookColor = bookColorMap[book.category] ?: ElectricGold
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .height(130.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) bookColor else bookColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedBookId = if (isSelected) null else book.id
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CosmicSpaceBg else CosmicSlateCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(book.emoji, fontSize = 20.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(bookColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(book.category, color = bookColor, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                Column {
                                    Text(
                                        text = book.title,
                                        color = SteelGrayText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "by " + book.author,
                                        color = MutedSlate,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected book details expanded panel
                val matchedBook = remember(selectedBookId) {
                    BookLibrary.books.find { it.id == selectedBookId }
                }

                matchedBook?.let { book ->
                    val themeColor = bookColorMap[book.category] ?: ElectricGold
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(book.emoji, fontSize = 16.sp)
                                    Text(
                                        text = book.title.uppercase(),
                                        color = themeColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = "Book Intel Active",
                                    color = MasteredGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = themeColor.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "CORE HIGHLIGHTED TAKEAWAY:",
                                color = themeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = book.takeaway,
                                color = SteelGrayText,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.callGeminiAI("BOOK_WISDOM", book.id)
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "DEPLOY ANALYTICS ⚔️",
                                        color = CosmicSpaceBg,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        aiQuery = "Analyze concept from ${book.title} (Takeaway: ${book.takeaway}) - "
                                    },
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .border(1.dp, MutedSlate.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSpaceBg),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "LOAD INPUT 📝",
                                        color = SteelGrayText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECURE GEOMETRIC MULTI-AI COACH SECTOR
        // ==========================================
        item {
            Text(
                text = "AI COMMAND COORDINATOR (DECK V3.5)",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            val geminiResult by viewModel.geminiResult.collectAsState()
            val geminiLoading by viewModel.geminiLoading.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("multi_ai_command_deck"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Coordinate study blocks by choosing specific AI App architectures. Each engine is simulated and stylized dynamically.",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Engine Selection Header
                    Text(
                        text = "SELECT ACTIVE AI BRAIN:",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 1. SELECT ENGINE CHIPS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiEngine.values().forEach { engine ->
                            val isSelected = selectedEngine == engine
                            val engineName = when (engine) {
                                AiEngine.GEMINI -> "Gemini"
                                AiEngine.CLAUDE -> "Claude"
                                AiEngine.CHATGPT -> "ChatGPT"
                                AiEngine.NOTEBOOK_LM -> "Notebook"
                            }
                            val engineColor = when (engine) {
                                AiEngine.GEMINI -> ElectricGold
                                AiEngine.CLAUDE -> Color(0xFFFF8A65) // Warm Peach/Rust
                                AiEngine.CHATGPT -> Color(0xFF4DB6AC) // Teal
                                AiEngine.NOTEBOOK_LM -> Color(0xFF4FC3F7) // light blue
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) engineColor.copy(alpha = 0.15f) else CosmicSpaceBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) engineColor else MutedSlate.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setAiEngine(engine) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = engineName,
                                    color = if (isSelected) engineColor else SteelGrayText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. TONE/VOICE STYLE SELECTOR
                    Text(
                        text = "ACTIVE STUDY MENTOR TONE:",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiVoiceStyle.values().forEach { voice ->
                            val isSelected = selectedVoice == voice
                            val voiceLabel = when (voice) {
                                AiVoiceStyle.STRICT_COMMANDER -> "Commander"
                                AiVoiceStyle.HELPFUL_MENTOR -> "Mentor"
                                AiVoiceStyle.MNEMONIC_WIZARD -> "Wizard"
                                AiVoiceStyle.SOCRATIC_DRILLER -> "Driller"
                            }
                            val voiceEmoji = when (voice) {
                                AiVoiceStyle.STRICT_COMMANDER -> "⚔️"
                                AiVoiceStyle.HELPFUL_MENTOR -> "📚"
                                AiVoiceStyle.MNEMONIC_WIZARD -> "🧠"
                                AiVoiceStyle.SOCRATIC_DRILLER -> "🔍"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MedicalCyan.copy(alpha = 0.15f) else CosmicSpaceBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MedicalCyan else MutedSlate.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setAiVoiceStyle(voice) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(text = voiceEmoji, fontSize = 9.sp)
                                    Text(
                                        text = voiceLabel,
                                        color = if (isSelected) MedicalCyan else SteelGrayText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Engine Status Label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicSpaceBg)
                            .padding(10.dp)
                    ) {
                        val activeStatusText = when (selectedEngine) {
                            AiEngine.GEMINI -> "Google Gemini Active Brain: Hardcore factual analysis referencing standard authors ('Reddy 34th Ed', 'K Park 26th Ed') with uncompromising accuracy."
                            AiEngine.CLAUDE -> "Anthropic Claude Active Brain: Clinical-grade logical pathway syntheses, comprehensive comparative charts, & patient-symptom diagnostic breakdowns."
                            AiEngine.CHATGPT -> "OpenAI ChatGPT Active Brain: Ultra high-speed active recall memory codes, acronyms, association shortcuts, and diagnostic checklist tables."
                            AiEngine.NOTEBOOK_LM -> "NotebookLM Active Brain: Scholarly Note-Clerk system focusing on study brief formulations, academic outline indexing, and citation structures."
                        }
                        Text(
                            text = activeStatusText,
                            fontSize = 11.sp,
                            color = SteelGrayText,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Quick Action Command Seeds
                    Text(
                        text = "RAPID SEED COMMANDS:",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.callGeminiAI("MNEMONIC", "Generate 5 critical medical mnemonics for FMT Toxicology stages")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MedicalCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSpaceBg),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Toxic Mnemonics 🧪", color = MedicalCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.callGeminiAI("DEEP_DIVE", "Draft 5 highly detailed and tough medical exam questions testing PSM Cold Chain & Sentinel Surveillance parameters")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ElectricGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSpaceBg),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Syllabus Drill 🎓", color = ElectricGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Standard Query Box
                    OutlinedTextField(
                        value = aiQuery,
                        onValueChange = { aiQuery = it },
                        placeholder = { Text("Enter prompt and trigger custom AI architectures...", color = MutedSlate.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_coach_query")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pill Suggestions
                    val quickPills = listOf("Organophosphorus poisoning", "Sentinel Surveillance", "TrueYield", "Asphyxial deaths")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPills.forEach { pill ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CosmicSpaceBg)
                                    .clickable { aiQuery = pill }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(pill, color = MedicalCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Process Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.callGeminiAI("DEEP_DIVE", aiQuery) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_ai_deepdive"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGold)
                        ) {
                            Text("Deep-Dive 📚", color = CosmicSpaceBg, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Button(
                            onClick = { viewModel.callGeminiAI("FACT_CHECK", aiQuery) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_ai_factcheck"),
                            colors = ButtonDefaults.buttonColors(containerColor = OptionGold)
                        ) {
                            Text("Fact-Check ✔", color = CosmicSpaceBg, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Button(
                            onClick = { viewModel.callGeminiAI("MNEMONIC", aiQuery) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_ai_mnemonic"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                        ) {
                            Text("Mnemonic 🧠", color = CosmicSpaceBg, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }

                    // --- Conversational Chat Bubble Stream ---
                    val chatHistory by viewModel.aiChatHistory.collectAsState()

                    if (chatHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ACTIVE SESSION COMMS TRANSCRIPT:",
                            fontWeight = FontWeight.Bold,
                            color = ElectricGold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            chatHistory.forEach { msg ->
                                val isUser = msg.sender == "USER"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    val bubbleBg = if (isUser) CosmicSpaceBg else when (msg.engine) {
                                        AiEngine.GEMINI -> Color(0xFF131A2D)
                                        AiEngine.CLAUDE -> Color(0xFF231B1A)
                                        AiEngine.CHATGPT -> Color(0xFF132223)
                                        AiEngine.NOTEBOOK_LM -> Color(0xFF1B182D)
                                    }
                                    val borderAccent = if (isUser) MutedSlate.copy(alpha = 0.3f) else when (msg.engine) {
                                        AiEngine.GEMINI -> ElectricGold
                                        AiEngine.CLAUDE -> Color(0xFFFF8A65)
                                        AiEngine.CHATGPT -> Color(0xFF4DB6AC)
                                        AiEngine.NOTEBOOK_LM -> Color(0xFF4FC3F7)
                                    }
                                    val senderLabel = if (isUser) "YOU CADET" else when (msg.engine) {
                                        AiEngine.GEMINI -> "GEMINI COMMAND 🔵"
                                        AiEngine.CLAUDE -> "CLAUDE PATHWAYS 🟠"
                                        AiEngine.CHATGPT -> "CHATGPT MEMORY 🟢"
                                        AiEngine.NOTEBOOK_LM -> "NOTEBOOKLM INDEX 🟣"
                                    }
                                    val mentorStyle = when (msg.voice) {
                                        AiVoiceStyle.STRICT_COMMANDER -> "⚔️ Commander"
                                        AiVoiceStyle.HELPFUL_MENTOR -> "📚 Mentor"
                                        AiVoiceStyle.MNEMONIC_WIZARD -> "🧠 Wizard"
                                        AiVoiceStyle.SOCRATIC_DRILLER -> "🔍 Driller"
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .border(1.dp, borderAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = senderLabel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = borderAccent
                                                )
                                                Text(
                                                    text = mentorStyle,
                                                    fontSize = 9.sp,
                                                    color = MutedSlate,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.text,
                                                color = SteelGrayText,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                fontFamily = if (isUser) FontFamily.Default else FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Text Button to Clear History
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.clearChatHistory() }) {
                                Text("CLEAR COMMS TRANSCRIPT 🧹", color = Color.Red.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (geminiLoading) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ElectricGold)
                        }
                    }

                    // Conversational Wizard Form to generate active recall card
                    val lastResponse = chatHistory.lastOrNull { !it.sender.equals("USER", true) }
                    if (lastResponse != null && !geminiLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        var saveSubject by remember { mutableStateOf("FMT") }
                        var saveQuestion by remember { mutableStateOf("Explain: ${aiQuery.ifEmpty { "Selected Concept" }}") }
                        var saveAnswer by remember { mutableStateOf("") }
                        var showWizard by remember { mutableStateOf(false) }
                        var hasSaved by remember { mutableStateOf(false) }

                        // Initialize save answer with response text once
                        LaunchedEffect(lastResponse) {
                            var cleanText = lastResponse.text
                            if (cleanText.length > 300) {
                                cleanText = cleanText.take(297) + "..."
                            }
                            saveAnswer = cleanText
                            hasSaved = false
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MasteredGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = CosmicSpaceBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("⚡", fontSize = 12.sp)
                                        Text(
                                            "ACTIVE FLASHCARD CONVERTER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MasteredGreen
                                        )
                                    }
                                    TextButton(onClick = { showWizard = !showWizard }) {
                                        Text(if (showWizard) "CLOSE" else "OPEN CONVERTER", color = ElectricGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (showWizard) {
                                    if (hasSaved) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("✓ Flashcard committed to SQLite Database. Roster updated live!", color = MasteredGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Instantly convert the last AI response block into a permanent spaced-repetition card.",
                                            color = MutedSlate,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Subject Row Selection
                                        Text("SUBJECT CATEGORY:", color = ElectricGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("FMT", "PSM", "FINANCE", "GENERAL").forEach { sub ->
                                                val isSubSelected = saveSubject == sub
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSubSelected) MedicalCyan.copy(alpha = 0.2f) else CosmicSlateCard)
                                                        .border(1.dp, if (isSubSelected) MedicalCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                                        .clickable { saveSubject = sub }
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                ) {
                                                    Text(sub, color = if (isSubSelected) MedicalCyan else SteelGrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Question
                                        Text("RECALL QUESTION:", color = ElectricGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = saveQuestion,
                                            onValueChange = { saveQuestion = it },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = SteelGrayText,
                                                unfocusedTextColor = SteelGrayText,
                                                focusedBorderColor = ElectricGold,
                                                unfocusedBorderColor = MutedSlate.copy(alpha = 0.3f),
                                                focusedContainerColor = CosmicSlateCard,
                                                unfocusedContainerColor = CosmicSlateCard
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("recall_convert_question"),
                                            singleLine = true
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Answer
                                        Text("RECALL ANSWER (AI BLOCK):", color = ElectricGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = saveAnswer,
                                            onValueChange = { saveAnswer = it },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = SteelGrayText,
                                                unfocusedTextColor = SteelGrayText,
                                                focusedBorderColor = ElectricGold,
                                                unfocusedBorderColor = MutedSlate.copy(alpha = 0.3f),
                                                focusedContainerColor = CosmicSlateCard,
                                                unfocusedContainerColor = CosmicSlateCard
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("recall_convert_answer")
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                viewModel.addNewFlashcard(
                                                    subject = saveSubject,
                                                    question = saveQuestion,
                                                    answer = saveAnswer,
                                                    explanation = "[Generated via Coach V3.5 under ${lastResponse.engine} Mode]"
                                                )
                                                hasSaved = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MasteredGreen)
                                        ) {
                                            Text("COMMIT TO ACTIVE RECALL ROSTER 💾", color = CosmicSpaceBg, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

private val OptionGold = Color(0xFFFFD54F)

// ==========================================
// TAB 3: HONOR ROLL LOGS (HISTORY INDEX)
// ==========================================
@Composable
fun HonorRollTab(logs: List<DailyLogEntity>, viewModel: FalconViewModel) {
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "THE CADET HONOR JOURNAL",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateCard)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No logs recorded yet. Begin your Command centers checklist!", color = MutedSlate)
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.loadLogForDate(log.date) },
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.date,
                                fontWeight = FontWeight.Bold,
                                color = ElectricGold,
                                fontSize = 14.sp
                            )

                            // Pillar checked count
                            var checkCount = 0
                            if (log.bodyChecked) checkCount++
                            if (log.mindChecked) checkCount++
                            if (log.honorChecked) checkCount++
                            if (log.holdChecked) checkCount++

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (checkCount == 4) MasteredGreen.copy(alpha = 0.2f) else CosmicSpaceBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$checkCount/4 Pillars Held",
                                    color = if (checkCount == 4) MasteredGreen else SteelGrayText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular indicators for pillars
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LogPillarIndicator("BODY", log.bodyChecked, MasteredGreen)
                            LogPillarIndicator("MIND", log.mindChecked, MedicalCyan)
                            LogPillarIndicator("HONOR", log.honorChecked, HoldAmber)
                            LogPillarIndicator("HOLD", log.holdChecked, FlinchRed)
                        }

                        if (log.diaryLine.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Diary: \"${log.diaryLine}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SteelGrayText,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 12.sp
                            )
                        }

                        // Study statistics
                        if (log.studyHoursFmt > 0f || log.studyHoursPsm > 0f) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "Study Blocks: FMT: %.1f hrs | PSM: %.1f hrs", log.studyHoursFmt, log.studyHoursPsm),
                                fontSize = 10.sp,
                                color = MedicalCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogPillarIndicator(name: String, active: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (active) color else MutedSlate.copy(alpha = 0.4f))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            color = if (active) color else MutedSlate,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}


// ==========================================
// TAB 4: FIRE & TRUEYIELD METRIC ENGINE
// ==========================================
@Composable
fun TrueYieldTab(viewModel: FalconViewModel, currentLog: DailyLogEntity?) {
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    var principalInput by remember { mutableStateOf("500000") } // Default ₹5 Lakh
    var sipInput by remember { mutableStateOf("50000") }       // Default ₹50k per month
    var targetInput by remember { mutableStateOf("10000000") } // Default ₹1 Crore milestone

    val currentPortfolio = principalInput.toDoubleOrNull() ?: 0.0
    val monthlySip = sipInput.toDoubleOrNull() ?: 0.0
    val targetVal = targetInput.toDoubleOrNull() ?: 10000000.0

    // TrueYield Formula constants
    val nominalCagr = 0.12 // 12% equity average standard index return
    val inflationRate = 0.06 // 6% average inflation
    val expenseRatio = 0.002 // 0.2% direct index fund cost
    val ltcgTaxDrag = 0.01 // ~1% drag due to 12.5% LTCG on redemption
    val trueYieldRate = nominalCagr - inflationRate - expenseRatio - ltcgTaxDrag // Net real return = 4.8%

    // Calculate months to FIRE milestone
    // Formula: Future Value of Principal + Future Value of Annuity (SIP) = Target
    // Simple numeric projection month-by-month
    val projectionMonths = remember(currentPortfolio, monthlySip, targetVal) {
        var current = currentPortfolio
        val r = trueYieldRate / 12.0
        var months = 0
        if (current >= targetVal) {
            0
        } else if (monthlySip <= 0.0 && r <= 0.0) {
            -1 // Infinite / never reached
        } else {
            while (current < targetVal && months < 600) { // Keep cap to 50 years to avoid crash
                current = current * (1.0 + r) + monthlySip
                months++
            }
            months
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "TRUEYIELD FIRE ENGINE (₹1 CRORE TARGET)",
                fontWeight = FontWeight.Bold,
                color = ElectricGold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )
        }

        // TrueYield definition card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = "Formula", tint = ElectricGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TrueYield Real-Rate Formula",
                            color = ElectricGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TrueYield = Nominal CAGR - (Inflation + Expense Ratio + LT Capital Gains Tax drag)\n" +
                               "12% (Equity) - 6% (Govt Inflation) - 0.2% (Direct Index) - 1.0% (LTCG drag) = Net Real Yield: 4.8% net compounding growth.",
                        color = SteelGrayText,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // INPUT SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "YOUR TRIAL VARIABLES",
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = principalInput,
                        onValueChange = { principalInput = it },
                        label = { Text("Current Index Portfolio Principal (₹)", color = MutedSlate) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("portfolio_val_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sipInput,
                        onValueChange = { sipInput = it },
                        label = { Text("Post-MBBS Monthly SIP Target (₹)", color = MutedSlate) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = { Text("Compounding FIRE Milestone Goal (₹)", color = MutedSlate) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SteelGrayText,
                            unfocusedTextColor = SteelGrayText,
                            focusedBorderColor = ElectricGold,
                            unfocusedBorderColor = MutedSlate.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // RESULTS & PROJECTIONS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formattedTarget = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(targetVal)
                    Text(
                        text = "ROADMAP TO $formattedTarget",
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (projectionMonths == -1) {
                        Text(
                            text = "Infinite Years",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = FlinchRed
                        )
                        Text(
                            text = "Increase monthly SIP contributions to cross targets.",
                            fontSize = 12.sp,
                            color = SteelGrayText
                        )
                    } else if (projectionMonths == 0) {
                        Text(
                            text = "Milestone Achieved!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MasteredGreen
                        )
                    } else {
                        val yearsDouble = projectionMonths / 12.0
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f Years", yearsDouble),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricGold
                        )
                        Text(
                            text = "(${projectionMonths} months of robust TrueYield compounding)",
                            fontSize = 11.sp,
                            color = SteelGrayText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicSpaceBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Note: Compounding relies purely on behavior over math. Standard Direct Index Funds compound reliably while keeping expenses at minimum. Protect your primary surplus (surplus margin).",
                            color = SteelGrayText,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: DISCIPLINE & ROUTINE HABIT SYSTEM
// ==========================================
fun calculateHabitStreak(doneDaysStr: String, selectedDateStr: String): Int {
    if (doneDaysStr.isBlank()) return 0
    val doneSet = doneDaysStr.split(",").filter { it.isNotBlank() }.toSet()
    if (!doneSet.contains(selectedDateStr)) return 0
    
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var currentStreak = 0
    try {
        var date = sdf.parse(selectedDateStr) ?: return 0
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (doneSet.contains(dateStr)) {
                currentStreak++
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return currentStreak
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisciplineTab(viewModel: FalconViewModel) {
    val allHabits by viewModel.allHabits.collectAsState()
    val cumulativeXp by viewModel.cumulativeXp.collectAsState()
    
    val ElectricGold = MaterialTheme.colorScheme.primary
    val MedicalCyan = MaterialTheme.colorScheme.secondary
    
    var selectedDate by remember { mutableStateOf(viewModel.currentDateString) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    
    // Form Input State
    var habitName by remember { mutableStateOf("") }
    var habitTime by remember { mutableStateOf("08:00") }
    var habitEmoji by remember { mutableStateOf("🎯") }
    var habitXp by remember { mutableStateOf(20) }
    var habitCategory by remember { mutableStateOf("DISCIPLINE") }
    var showCreator by remember { mutableStateOf(false) }
    
    // Level Calculation
    val userLevel = 1 + (cumulativeXp / 500)
    val xpInLevel = cumulativeXp % 500
    val xpProgress = xpInLevel.toFloat() / 500f
    
    val categories = listOf("ALL", "DISCIPLINE", "PHYSICAL", "COGNITIVE", "NUTRITION", "HEALTH", "MENTAL")
    
    // Simple date adjuster using Calendar
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val changeDate: (Int) -> Unit = { offset ->
        try {
            val date = sdf.parse(selectedDate) ?: java.util.Date()
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.add(java.util.Calendar.DAY_OF_YEAR, offset)
            selectedDate = sdf.format(cal.time)
        } catch(e: Exception) {}
    }
    
    // Filter habits of the user
    val filteredList = allHabits.filter {
        selectedCategory == "ALL" || it.cat.uppercase() == selectedCategory.uppercase()
    }
    
    // Ratio of today's completed items
    val totalOnDateCount = allHabits.size
    val doneOnDateCount = allHabits.count {
        val doneList = if (it.doneDays.isBlank()) emptyList<String>() else it.doneDays.split(",").filter { d -> d.isNotBlank() }
        doneList.contains(selectedDate)
    }
    val completionPercentage = if (totalOnDateCount > 0) (doneOnDateCount * 100) / totalOnDateCount else 0
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Tactical Header Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("discipline_header_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active Routine", tint = ElectricGold)
                            Text(
                                text = "OPERATION: DISCIPLINE",
                                fontWeight = FontWeight.Bold,
                                color = ElectricGold,
                                fontSize = 16.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                        
                        // Level Info badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MedicalCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "COGNITIVE LVL $userLevel",
                                fontSize = 10.sp,
                                color = MedicalCyan,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // XP Level progression bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Prestige: $cumulativeXp XP", fontSize = 12.sp, color = SteelGrayText, fontWeight = FontWeight.Bold)
                        Text(text = "$xpInLevel / 500 XP to Level Up", fontSize = 11.sp, color = MutedSlate)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = xpProgress,
                        color = ElectricGold,
                        trackColor = CosmicSpaceBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
        
        // Date Pick Selection Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { changeDate(-1) }) {
                        Text("◀", color = ElectricGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val displayDate = if (selectedDate == viewModel.currentDateString) "TODAY" else selectedDate
                        Text(
                            text = displayDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ElectricGold
                        )
                        Text(
                            text = "$doneOnDateCount / $totalOnDateCount Routines Completed ($completionPercentage%)",
                            fontSize = 11.sp,
                            color = MutedSlate
                        )
                    }
                    
                    IconButton(onClick = { changeDate(1) }) {
                        Text("▶", color = ElectricGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        
        // Add Objective Selector collapsible builder
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCreator = !showCreator }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TACTICAL OBJECTIVES INDEX",
                        fontWeight = FontWeight.Bold,
                        color = ElectricGold,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (showCreator) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Toggle Customizer",
                            tint = MedicalCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (showCreator) "CLOSE ADAPTER" else "ADD OBJECTIVE",
                            fontSize = 11.sp,
                            color = MedicalCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (showCreator) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("COMMENCE NEW HABIT / OBJECTIVE", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MedicalCyan)
                            
                            OutlinedTextField(
                                value = habitName,
                                onValueChange = { habitName = it },
                                label = { Text("Objective Name (e.g. Study FMT)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MedicalCyan,
                                    unfocusedBorderColor = MutedSlate,
                                    focusedLabelColor = MedicalCyan,
                                    unfocusedLabelColor = MutedSlate
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = habitTime,
                                    onValueChange = { habitTime = it },
                                    label = { Text("Scheduled Time") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalCyan, unfocusedBorderColor = MutedSlate)
                                )
                                OutlinedTextField(
                                    value = habitEmoji,
                                    onValueChange = { habitEmoji = it },
                                    label = { Text("Icon/Emoji") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalCyan, unfocusedBorderColor = MutedSlate)
                                )
                            }
                            
                            // Category Buttons Selector row
                            Text("CATEGORY FOCUS SECTION", fontSize = 10.sp, color = MutedSlate, fontWeight = FontWeight.Bold)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val categoriesToPick = listOf("DISCIPLINE", "PHYSICAL", "COGNITIVE", "NUTRITION", "HEALTH", "MENTAL")
                                items(categoriesToPick) { cat ->
                                    val active = habitCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) MedicalCyan else CosmicSpaceBg)
                                            .clickable { habitCategory = cat }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (active) CosmicSpaceBg else SteelGrayText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Slider for XP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Earn Value: +$habitXp XP", fontSize = 12.sp, color = SteelGrayText, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = habitXp.toFloat(),
                                    onValueChange = { habitXp = it.toInt() },
                                    valueRange = 5f..50f,
                                    steps = 9,
                                    modifier = Modifier.width(180.dp),
                                    colors = SliderDefaults.colors(thumbColor = MedicalCyan, activeTrackColor = MedicalCyan)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    if (habitName.isNotBlank()) {
                                        viewModel.addCustomHabit(habitName, habitCategory, habitTime, habitEmoji, habitXp)
                                        habitName = ""
                                        showCreator = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                            ) {
                                Text("SECURE ON BOARD 💾", color = CosmicSpaceBg, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        
        // Category Navigation Pills Row
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val active = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) ElectricGold else CosmicSlateCard)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (active) CosmicSpaceBg else SteelGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Interactive habits list
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No routines matching category filters.",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredList) { habit ->
                val doneDays = if (habit.doneDays.isBlank()) emptyList<String>() else habit.doneDays.split(",").filter { it.isNotBlank() }
                val isDone = doneDays.contains(selectedDate)
                val streak = calculateHabitStreak(habit.doneDays, selectedDate)
                
                // Category pill theme colors
                val catColor = when (habit.cat.uppercase()) {
                    "DISCIPLINE" -> Color(0xFFF59E0B)
                    "PHYSICAL" -> Color(0xFFEF4444)
                    "COGNITIVE" -> Color(0xFF3B82F6)
                    "NUTRITION" -> Color(0xFF10B981)
                    "HEALTH" -> Color(0xFF06B6D4)
                    else -> Color(0xFF8B5CF6)
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isDone) catColor.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Emoji asset
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicSpaceBg),
                                contentAlignment = Alignment.Center
                              ) {
                                Text(habit.icon, fontSize = 20.sp)
                            }
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = habit.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) MutedSlate else ElectricGold,
                                        fontSize = 13.sp,
                                        style = if (isDone) MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ) else MaterialTheme.typography.bodyMedium
                                    )
                                    if (habit.id.startsWith("h_")) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Habit",
                                            tint = FlinchRed.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.deleteHabit(habit.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Cat tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(catColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = habit.cat,
                                            fontSize = 8.sp,
                                            color = catColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = habit.time,
                                        fontSize = 10.sp,
                                        color = MutedSlate
                                    )
                                    if (streak > 0) {
                                        Text(
                                            text = "🔥 ${streak}d Streak",
                                            fontSize = 10.sp,
                                            color = Color(0xFFF59E0B),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Completion action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "+${habit.xp} XP", fontSize = 11.sp, color = catColor, fontWeight = FontWeight.Bold)
                            
                            Button(
                                onClick = { viewModel.toggleHabitCompletion(habit.id, selectedDate) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDone) catColor else CosmicSpaceBg
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isDone) "SECURED 🛡️" else "PENDING ⚔️",
                                    color = if (isDone) CosmicSpaceBg else SteelGrayText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
