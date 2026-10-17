package com.meh.projectgenesis.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.core.backup.ImportResult
import com.meh.projectgenesis.core.security.KeyManager
import com.meh.projectgenesis.domain.model.ReasoningMode
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: String,
    currentTheme: String,
    currentReasoningMode: ReasoningMode,
    currentAiProvider: String,
    keyManager: KeyManager,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onReasoningModeChange: (ReasoningMode) -> Unit,
    onAiProviderChange: (String) -> Unit,
    onSaveGeminiKey: (String) -> Unit,
    onClearGeminiKey: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onExportBackup: suspend () -> String,
    onImportBackup: suspend (String) -> ImportResult
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showKeyDialog by remember { mutableStateOf(false) }
    var inputApiKey by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var reasoningMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "সেটিংস ও নিরাপত্তা",
            subtitle = "গোপনীয়তা, এআই প্রোভাইডার ও ব্যাকআপ"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Language & Theme Section
            item {
                SectionTitle("অ্যাপ ইন্টারফেস ও ভাষা")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "ভাষা (Language)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row {
                                OutlinedButton(
                                    onClick = { onLanguageChange("bn") },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (currentLanguage == "bn") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("বাংলা", color = if (currentLanguage == "bn") GenesisCyanAccent else MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { onLanguageChange("en") },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (currentLanguage == "en") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("English", color = if (currentLanguage == "en") GenesisCyanAccent else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "থিম মোড", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row {
                                OutlinedButton(
                                    onClick = { onThemeChange("DARK") },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (currentTheme == "DARK") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("ডার্ক", color = if (currentTheme == "DARK") GenesisCyanAccent else MaterialTheme.colorScheme.onSurface)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { onThemeChange("LIGHT") },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (currentTheme == "LIGHT") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("লাইট", color = if (currentTheme == "LIGHT") GenesisCyanAccent else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reasoning Mode Configuration
            item {
                SectionTitle("যুক্তিবাদী সিদ্ধান্ত মোড")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "সক্রিয় মোড: ${currentReasoningMode.titleBn} (${currentReasoningMode.titleEn})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GenesisCyanAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentReasoningMode.descriptionBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = reasoningMenuExpanded,
                            onExpandedChange = { reasoningMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = currentReasoningMode.titleBn,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("৮টি রিজনিং মোড নির্বাচন") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasoningMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = reasoningMenuExpanded,
                                onDismissRequest = { reasoningMenuExpanded = false }
                            ) {
                                ReasoningMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(mode.titleBn, fontWeight = FontWeight.Bold)
                                                Text(mode.descriptionBn, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            onReasoningModeChange(mode)
                                            reasoningMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI Provider & Key Management Section
            item {
                SectionTitle("এআই প্রোভাইডার ও এপিআই কি")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "বর্তমান প্রোভাইডার: ${if (currentAiProvider == "GEMINI") "Google Gemini API" else "Genesis Local Engine (অফলাইন ডেমো)"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onAiProviderChange("LOCAL") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentAiProvider == "LOCAL") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("লোকাল ইঞ্জিন", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onAiProviderChange("GEMINI") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentAiProvider == "GEMINI") GenesisCyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("Gemini API", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Status & Warning
                        Text(
                            text = "সংরক্ষিত Gemini কি: ${keyManager.getMaskedGeminiKey()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GenesisCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Security Warning
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GenesisIndigoDeep
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = GenesisWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সতর্কতা: ক্লায়েন্ট-সাইড কি রিভার্স ইঞ্জিনিয়ারিং দ্বারা নিষ্কাশন সম্ভব। কোনো কি ছাড়া অ্যাপটি লোকাল রিজনিং মোডে নিরাপদভাবে কাজ করবে। ব্যাকআপ থেকে কি সম্পূর্ণ বর্জিত।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (keyManager.getGeminiApiKey() != null) {
                                OutlinedButton(
                                    onClick = {
                                        onClearGeminiKey()
                                        Toast.makeText(context, "এপিআই কি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                                ) {
                                    Text("কি মুছুন")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = { showKeyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GenesisCyanAccent, contentColor = Color.Black)
                            ) {
                                Text("কি কনফিগার করুন")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = onOpenDiagnostics,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_open_diagnostics"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GenesisCyanAccent)
                        ) {
                            Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("এআই ইঞ্জিন ডায়াগনস্টিকস ও আর্কিটেকচার অডিট")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Backup & Export / Import
            item {
                SectionTitle("ব্যাকআপ ও ডেটা পুনরুদ্ধার")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "সংস্করণ ১.০ কাঠামোগত JSON ব্যাকআপ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ইউটিএফ-৮ বাংলা পাঠ্য সম্পূর্ণ অক্ষুণ্ণ রেখে সকল মেমোরি, প্রজেক্ট ও টাস্ক ব্যাকআপ করে। কোনো এপিআই কি বা গোপনীয় তথ্য ব্যাকআপে যুক্ত হয় না।\n\n• SHA-256 ইন্টিগ্রিটি চেকসাম (Integrity Checksum): ব্যাকআপ ফাইল বিকৃতি বা অসম্পূর্ণ ডাউনলোড সনাক্ত করে। এটি এনক্রিপশন (Encryption) বা সত্যতা প্রমাণীকরণ (Authentication) নয়।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val json = onExportBackup()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Genesis JSON Backup", json))
                                        Toast.makeText(context, "JSON ব্যাকআপ ক্লিপবোর্ডে কপি করা হয়েছে (${json.length} অক্ষর)", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("JSON এক্সপোর্ট", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showImportDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GenesisCyanAccent, contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ইমপোর্ট / রিস্টোর", fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Configure API Key Dialog
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = { Text("Gemini API Key সেট করুন") },
            text = {
                Column {
                    Text(
                        text = "আপনার ব্যক্তিগত কি প্রদান করুন (ঐচ্ছিক)। কোনো কি না দিলে লোকাল ইঞ্জিন ব্যবহৃত হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputApiKey.isNotBlank()) {
                            onSaveGeminiKey(inputApiKey.trim())
                            inputApiKey = ""
                            showKeyDialog = false
                            Toast.makeText(context, "কি সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GenesisCyanAccent, contentColor = Color.Black)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("JSON ব্যাকআপ ইমপোর্ট") },
            text = {
                Column {
                    Text(
                        text = "পূর্বে এক্সপোর্টকৃত প্রজেক্ট জেনেসিস JSON টেক্সট পেস্ট করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("{\"appId\":\"PROJECT_GENESIS_BACKUP\"...}") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            coroutineScope.launch {
                                val result = onImportBackup(importJsonText)
                                if (result.success) {
                                    Toast.makeText(
                                        context,
                                        "ইমপোর্ট সফল! ${result.memoriesImported}টি মেমোরি, ${result.projectsImported}টি প্রজেক্ট পুনরুদ্ধারকৃত (${result.memoriesSkippedDuplicates}টি ডুপ্লিকেট স্কিপ)।",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showImportDialog = false
                                    importJsonText = ""
                                } else {
                                    Toast.makeText(context, result.errorMessage ?: "ইমপোর্টে ত্রুটি", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GenesisCyanAccent, contentColor = Color.Black)
                ) {
                    Text("পুনরুদ্ধার করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
