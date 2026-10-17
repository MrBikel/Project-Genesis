package com.meh.projectgenesis.ui.screens.actions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.data.local.ActionProposalEntity
import com.meh.projectgenesis.domain.model.ActionStatus
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.ui.components.AutonomyBadge
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCenterScreen(
    currentAutonomy: AutonomyLevel,
    isEmergencyStopActive: Boolean,
    pendingActions: List<ActionProposalEntity>,
    allActions: List<ActionProposalEntity>,
    onSetAutonomyLevel: (AutonomyLevel) -> Unit,
    onToggleEmergencyStop: () -> Unit,
    onApproveAction: (String) -> Unit,
    onRejectAction: (String) -> Unit,
    onRollbackAction: (String) -> Unit
) {
    var autonomyMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "অ্যাকশন ও পারমিশন সেন্টার",
            subtitle = "স্বায়ত্তশাসন ও নিরাপত্তা নিয়ন্ত্রণ"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Emergency Stop Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEmergencyStopActive) Color(0xFF5C1010) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEmergencyStopActive) Color(0xFFFF5252) else MaterialTheme.colorScheme.outlineVariant
                    )
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isEmergencyStopActive) Color(0xFFFF5252) else GenesisIndigoDeep),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isEmergencyStopActive) Icons.Default.Pause else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isEmergencyStopActive) Color.White else GenesisCyanAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ইমার্জেন্সি স্টপ (Emergency Stop)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isEmergencyStopActive) "সকল অ্যাকশন অবিলম্বে স্থগিত রয়েছে" else "সিস্টেম স্বাভাবিক অবস্থায় চলমান",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isEmergencyStopActive) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onToggleEmergencyStop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEmergencyStopActive) GenesisSuccess else Color(0xFFFF5252),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_toggle_emergency_stop")
                        ) {
                            Icon(
                                imageVector = if (isEmergencyStopActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEmergencyStopActive) "ইমার্জেন্সি স্টপ নিষ্ক্রিয় করুন" else "জরুরি বিরতি সক্রিয় করুন (Emergency Freeze)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Autonomy Level Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "বর্তমান স্বায়ত্তশাসন স্তর (Autonomy Level)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "স্তর ০ থেকে ৬ পর্যন্ত অনুমতি নিয়ন্ত্রণ করুন। সংবেদনশীল বাহ্যিক কাজের জন্য পৃথক নিশ্চিতকরণ আবশ্যক।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = autonomyMenuExpanded,
                            onExpandedChange = { autonomyMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${currentAutonomy.titleBn}",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = autonomyMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = autonomyMenuExpanded,
                                onDismissRequest = { autonomyMenuExpanded = false }
                            ) {
                                AutonomyLevel.entries.forEach { level ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = level.titleBn, fontWeight = FontWeight.Bold)
                                                Text(text = level.descriptionBn, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            onSetAutonomyLevel(level)
                                            autonomyMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onSetAutonomyLevel(AutonomyLevel.LEVEL_0) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9100)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সকল বিশেষ অনুমতি প্রত্যাহার করুন (Revoke All)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Pending Actions Header
            item {
                Text(
                    text = "পর্যালোচনার অপেক্ষায় প্রস্তাবিত অ্যাকশন (${pendingActions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (pendingActions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো অপেক্ষমাণ সংবেদনশীল অ্যাকশন নেই।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                items(pendingActions, key = { it.id }) { action ->
                    PendingActionCard(
                        action = action,
                        onApprove = { onApproveAction(action.id) },
                        onReject = { onRejectAction(action.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // History / Audit Log Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GenesisCyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যাকশন অডিট হিস্ট্রি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(allActions.filter { it.status != ActionStatus.PENDING_APPROVAL.name }, key = { it.id }) { action ->
                ActionHistoryCard(
                    action = action,
                    onRollback = { onRollbackAction(action.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PendingActionCard(
    action: ActionProposalEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GenesisWarning)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "উদ্দেশ্য: ${action.purpose}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "প্রভাবিত ডেটা: ${action.affectedData}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "ঝুঁকি: ${action.risk}", style = MaterialTheme.typography.bodySmall, color = GenesisWarning, fontWeight = FontWeight.SemiBold)
            Text(text = "বাতিলযোগ্যতা: ${if (action.isReversible) "হ্যাঁ (রোলব্যাক সম্ভব)" else "না (অপরিবর্তনীয়)"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))) {
                    Text("প্রত্যাখ্যান")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = GenesisSuccess, contentColor = Color.Black)) {
                    Text("অনুমোদন দিন")
                }
            }
        }
    }
}

@Composable
private fun ActionHistoryCard(
    action: ActionProposalEntity,
    onRollback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "স্ট্যাটাস: ${action.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!action.isReversible) {
                    Text(
                        text = "রোলব্যাক অনুপলব্ধ (অপরিবর্তনীয় পদক্ষেপ / Rollback unavailable)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF5252).copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "রোলব্যাক উপলব্ধ (Rollback available)",
                        style = MaterialTheme.typography.labelSmall,
                        color = GenesisCyanAccent,
                        fontSize = 10.sp
                    )
                }
            }

            if (action.isReversible && action.status == ActionStatus.APPROVED.name) {
                OutlinedButton(
                    onClick = onRollback,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B0FF)),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রোলব্যাক সম্পাদন", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
