package com.meh.projectgenesis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.EvidenceLevel
import com.meh.projectgenesis.domain.model.TaskStatus
import com.meh.projectgenesis.ui.theme.EvidenceE1Color
import com.meh.projectgenesis.ui.theme.EvidenceE2Color
import com.meh.projectgenesis.ui.theme.EvidenceE3Color
import com.meh.projectgenesis.ui.theme.EvidenceE4Color
import com.meh.projectgenesis.ui.theme.EvidenceE5Color
import com.meh.projectgenesis.ui.theme.EvidenceE6Color
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisNavyDark
import com.meh.projectgenesis.ui.theme.GenesisNavySurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesisTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GenesisCyanAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun GenesisBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple("home", "হোম", Icons.Default.Home),
            Triple("conversation", "কথোপকথন", Icons.Default.ChatBubble),
            Triple("memory", "মেমোরি", Icons.Default.Memory),
            Triple("projects", "প্রজেক্ট", Icons.Default.Folder),
            Triple("tasks", "টাস্ক", Icons.Default.Assignment),
            Triple("actions", "অ্যাকশন", Icons.Default.Security),
            Triple("settings", "সেটিংস", Icons.Default.Settings)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GenesisCyanAccent,
                    selectedTextColor = GenesisCyanAccent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_item_$route")
            )
        }
    }
}

@Composable
fun EvidenceBadge(level: EvidenceLevel) {
    val (color, label) = when (level) {
        EvidenceLevel.E1 -> EvidenceE1Color to "E1: যাচাইকৃত"
        EvidenceLevel.E2 -> EvidenceE2Color to "E2: নির্ভরযোগ্য"
        EvidenceLevel.E3 -> EvidenceE3Color to "E3: ব্যবহারকারী রেকর্ড"
        EvidenceLevel.E4 -> EvidenceE4Color to "E4: ব্যবহারকারী অভিজ্ঞতা"
        EvidenceLevel.E5 -> EvidenceE5Color to "E5: জেনেসিস বিশ্লেষণ"
        EvidenceLevel.E6 -> EvidenceE6Color to "E6: অনুমান"
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = Modifier.testTag("evidence_badge_${level.code}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AutonomyBadge(level: AutonomyLevel) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.testTag("autonomy_badge_${level.level}")
    ) {
        Text(
            text = level.titleBn,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TaskStatusChip(status: TaskStatus) {
    val chipColor = when (status) {
        TaskStatus.COMPLETED, TaskStatus.APPROVED -> Color(0xFF00E676)
        TaskStatus.IMPLEMENTATION, TaskStatus.TESTING -> GenesisCyanAccent
        TaskStatus.PLAN, TaskStatus.RESEARCH -> Color(0xFF00B0FF)
        TaskStatus.PAUSED, TaskStatus.CLOSED, TaskStatus.ARCHIVED -> Color(0xFF94A3B8)
        else -> Color(0xFFFFB300)
    }

    Box(
        modifier = Modifier
            .border(1.dp, chipColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .background(chipColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("task_status_${status.name}")
    ) {
        Text(
            text = status.titleBn,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = chipColor
        )
    }
}
