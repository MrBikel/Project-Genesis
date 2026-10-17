package com.meh.projectgenesis.ui.screens.diagnostics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.core.ai.EngineDiagnostics
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning

@Composable
fun DiagnosticsScreen(
    diagnostics: EngineDiagnostics?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "এআই ইঞ্জিন ডায়াগনস্টিকস",
            subtitle = "স্বচ্ছ আর্কিটেকচার নিরীক্ষণ ও অডিট",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Engine Classification Notice (Honest declaration)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_engine_classification"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GenesisCyanAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GenesisCyanAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ইঞ্জিন আর্কিটেকচার শ্রেণীকরণ (Audit Classification)",
                            style = MaterialTheme.typography.labelMedium,
                            color = GenesisCyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = diagnostics?.engineClassification ?: "B. Deterministic Rule-Based & Structured Templates (Local Default)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "নীতিগত স্পষ্টতা: জেনেসিস স্থানীয় রুল-বেসড বা টেমপ্লেট ভিত্তিক আউটপুটকে কখনোই বিভ্রান্তিকরভাবে মডেল-জেনারেটেড এআই দাবি করে না। Gemini API সক্রিয় থাকলে এটি মডেল-পাওয়ার্ড, অন্যথায় অফলাইন রুল-বেসড পাইপলাইন হিসেবে কাজ করে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diagnostic Parameters List
            Text(
                text = "সিস্টেম প্যারামিটার অডিট",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "সক্রিয় রিজনিং প্রোভাইডার",
                value = diagnostics?.activeProviderName ?: "Genesis Local Engine",
                description = "বর্তমান যুক্তি বিশ্লেষণ পরিচালনাকারী ইঞ্জিন।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "সক্রিয় মডেল নাম",
                value = diagnostics?.activeModelName ?: "Genesis-LocalRules-v1",
                description = "রিমোট মডেল বা অফলাইন রুলসেট সংস্করণ।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "এক্সিকিউশন মোড (স্থানীয় নাকি রিমোট)",
                value = if (diagnostics?.isRemoteExecution == true) "রিমোট নেটওয়ার্ক এক্সিকিউশন (Remote Cloud API)" else "অন-ডিভাইস লোকাল এক্সিকিউশন (On-Device Local)",
                description = "ডেটা সম্পূর্ণ ডিভাইসের মধ্যে প্রসেস হচ্ছে নাকি রিমোট ক্লাউডে।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "এপিআই প্রাপ্যতা (API Availability)",
                value = if (diagnostics?.isApiAvailable == true) "কনফিগারকৃত ও উপলব্ধ" else "অনুপলব্ধ (কোনো ক্লায়েন্ট কি সক্রিয় নেই)",
                description = "Gemini বা বহিরাগত এপিআই ক্রেডেনশিয়াল স্থিতি।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "সর্বশেষ প্রোভাইডার রিকোয়েস্ট স্থিতি",
                value = diagnostics?.lastProviderRequestStatus ?: "Local Execution Success",
                description = "সার্ভার রেসপন্স কোড বা লোকাল রুল এক্সিকিউশন রিপোর্ট।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "রেসপন্স নরমালাইজেশন ফলাফল",
                value = diagnostics?.lastResponseNormalizationResult ?: "Standard local rule pipeline",
                description = "মডেলের কাঁচা আউটপুট থেকে জেনেসিস ফরম্যাটে রূপান্তর প্রক্রিয়া।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "ডেমো / অফলাইন মোড স্থিতি",
                value = if (diagnostics?.isDemoModeActive != false) "সক্রিয় (Demo/Offline Mode)" else "নিষ্ক্রিয় (Live Remote Inference)",
                description = "কোনো পেমেন্ট বা গোপনীয় কি ছাড়া ব্যবহারের ডেমো মোড।"
            )

            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticItemCard(
                label = "আউটপুট টেমপ্লেটেড নাকি মডেল-পাওয়ার্ড",
                value = if (diagnostics?.isOutputMockedOrTemplated != false) "টেমপ্লেটেড ও রুল-ভিত্তিক (Deterministic)" else "প্রকৃত মডেল-জেনারেটেড (Genuinely Model-Powered)",
                description = "বর্তমান উত্তরের উৎপাদন পদ্ধতি।"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DiagnosticItemCard(
    label: String,
    value: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
