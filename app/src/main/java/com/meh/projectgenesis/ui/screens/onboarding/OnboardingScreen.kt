package com.meh.projectgenesis.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Emblem
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GenesisIndigoDeep)
                .border(2.dp, GenesisCyanAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "প্রজেক্ট জেনেসিস লোগো",
                tint = GenesisCyanAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PROJECT GENESIS",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = GenesisCyanAccent,
            letterSpacing = 2.sp,
            modifier = Modifier.testTag("onboarding_title")
        )

        Text(
            text = "বুদ্ধিবৃত্তিক যুক্তি সহযোগী ও মেমোরি সমৃদ্ধ ব্যক্তিগত এআই",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = { onLanguageChange("bn") },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (currentLanguage == "bn") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("btn_lang_bn")
            ) {
                Text(
                    text = "বাংলা (Bangla)",
                    fontWeight = if (currentLanguage == "bn") FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(
                onClick = { onLanguageChange("en") },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (currentLanguage == "en") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("btn_lang_en")
            ) {
                Text(
                    text = "English",
                    fontWeight = if (currentLanguage == "en") FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Principles Cards
        PrincipleItem(
            icon = Icons.Default.Psychology,
            title = "জেনেসিস কী এবং কী নয়",
            description = "জেনেসিস সাধারণ চ্যাটবট, অন্ধ স্তাবক বা কৃত্রিম সাধারণ বুদ্ধিমত্তা (AGI) নয়। এটি প্রমাণের ভিত্তিতে শ্রদ্ধাশীল দ্বিমত প্রকাশ করতে সক্ষম একটি স্বাধীন যুক্তি সহযোগী।"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrincipleItem(
            icon = Icons.Default.Storage,
            title = "নিয়ন্ত্রিত ও সুরক্ষিত মেমোরি",
            description = "আপনার ক্ষণস্থায়ী আবেগ, কৌতুক বা অযাচাইকৃত দাবি কখনোই স্থায়ী মেমোরি হবে না। ৯টি শ্রেণীতে কাঠামোগত মেমোরি শুধুমাত্র আপনার স্পষ্ট অনুমোদনেই সংরক্ষিত হয়।"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrincipleItem(
            icon = Icons.Default.Security,
            title = "স্থানীয় গোপনীয়তা ও স্পষ্ট অনুমতি",
            description = "সম্পূর্ণ অফলাইন-ফার্স্ট রুম ডাটাবেসে তথ্য সংরক্ষিত। আপনার অনুমতি ছাড়া কোনো ফাইল মুছে ফেলা, বার্তা পাঠানো বা তথ্য প্রকাশ কঠোরভাবে নিষিদ্ধ।"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_accept_onboarding"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GenesisCyanAccent,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "শর্তাবলী স্বীকার করে প্রবেশ করুন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PrincipleItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GenesisIndigoDeep),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GenesisCyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
