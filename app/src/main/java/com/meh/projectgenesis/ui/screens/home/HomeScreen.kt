package com.meh.projectgenesis.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.Project
import com.meh.projectgenesis.domain.model.ReasoningMode
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning

@Composable
fun HomeScreen(
    activeProject: Project?,
    activeReasoningMode: ReasoningMode,
    autonomyLevel: AutonomyLevel,
    isEmergencyStopActive: Boolean,
    candidateMemoriesCount: Int,
    pendingTasksCount: Int,
    pendingActionsCount: Int,
    onNavigate: (String) -> Unit,
    onQuickSend: (String) -> Unit
) {
    var quickInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "জেনেসিস হাব",
            subtitle = "বুদ্ধিবৃত্তিক অংশীদার"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Greeting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(GenesisSuccess)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সিস্টেম প্রস্তুত • অফলাইন সুরক্ষিত",
                                style = MaterialTheme.typography.labelMedium,
                                color = GenesisSuccess
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GenesisIndigoDeep
                        ) {
                            Text(
                                text = activeReasoningMode.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = GenesisCyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "শুভ দিন, আপনার স্বাধীন যুক্তি সহযোগী প্রস্তুত।",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "যেকোনো জটিল সিদ্ধান্ত, প্রজেক্ট পরিকল্পনা বা যুক্তি বিশ্লেষণের জন্য আপনি প্রস্তুত হলে প্রশ্ন করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Emergency Stop Alert Banner if active
            if (isEmergencyStopActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("actions") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A1010)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "জরুরি বিরতি (Emergency Stop) সক্রিয়! সকল অ্যাকশন স্থগিত রয়েছে। পরিবর্তন করতে স্পর্শ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFCDD2),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Project Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("projects") },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = GenesisCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সক্রিয় প্রজেক্ট প্রসঙ্গ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "পরিবর্তন করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = GenesisCyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = activeProject?.name ?: "কোনো প্রজেক্ট নির্বাচিত নেই (সাধারণ প্রসঙ্গ)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (activeProject != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeProject.goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "লার্নিং রিভিউ",
                    count = candidateMemoriesCount.toString(),
                    subtitle = "অনুমোদনের অপেক্ষায়",
                    icon = Icons.Default.Memory,
                    accentColor = GenesisCyanAccent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("learning") }
                        .testTag("home_card_learning")
                )

                StatCard(
                    title = "সক্রিয় টাস্ক",
                    count = pendingTasksCount.toString(),
                    subtitle = "অপেক্ষমাণ কাজ",
                    icon = Icons.Default.Assignment,
                    accentColor = Color(0xFF00B0FF),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("tasks") }
                        .testTag("home_card_tasks")
                )

                StatCard(
                    title = "অ্যাকশন রিভিউ",
                    count = pendingActionsCount.toString(),
                    subtitle = "অনুমতি যাচাই",
                    icon = Icons.Default.Shield,
                    accentColor = GenesisWarning,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("actions") }
                        .testTag("home_card_actions")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Reasoning Query Bar
            Text(
                text = "দ্রুত যুক্তি ও বিশ্লেষণ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = quickInput,
                onValueChange = { quickInput = it },
                placeholder = {
                    Text(
                        text = "প্রশ্ন বা প্রস্তাব লিখুন (যেমন: এই দাবিটির সত্যতা যাচাই করো...)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_quick_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GenesisCyanAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (quickInput.isNotBlank()) {
                                onQuickSend(quickInput)
                                quickInput = ""
                                onNavigate("conversation")
                            }
                        },
                        enabled = quickInput.isNotBlank(),
                        modifier = Modifier.testTag("home_quick_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "পাঠান",
                            tint = if (quickInput.isNotBlank()) GenesisCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Deep Dive Navigation Cards
            Text(
                text = "প্রধান মডিউলসমূহ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModuleItem(
                title = "মেমোরি কন্ট্রোল সেন্টার",
                description = "৯টি মেমোরি শ্রেণি পরিদর্শন, অনুসন্ধান, সম্পাদনা ও ব্যাকআপ এক্সপোর্ট।",
                icon = Icons.Default.Memory,
                onClick = { onNavigate("memory") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModuleItem(
                title = "রিজনিং পাইপলাইন ভিউ",
                description = "ইনটেক থেকে ক্রMessage রিভিউ পর্যন্ত ১১-ধাপের অভ্যন্তরীণ যুক্তি ডায়াগনস্টিকস।",
                icon = Icons.Default.Analytics,
                onClick = { onNavigate("reasoning") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModuleItem(
                title = "অ্যাকশন ও পারমিশন সেন্টার",
                description = "স্তর ০ থেকে ৬ পর্যন্ত কার্যক্ষমতা নিয়ন্ত্রণ ও ইমার্জেন্সি স্টপ সুবিধা।",
                icon = Icons.Default.Shield,
                onClick = { onNavigate("actions") }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun ModuleItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GenesisIndigoDeep),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GenesisCyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
