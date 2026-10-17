package com.meh.projectgenesis.ui.screens.memory

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.meh.projectgenesis.domain.model.EvidenceLevel
import com.meh.projectgenesis.domain.model.MemoryClass
import com.meh.projectgenesis.domain.model.MemoryRecord
import com.meh.projectgenesis.domain.model.MemoryStatus
import com.meh.projectgenesis.domain.model.SensitivityLevel
import com.meh.projectgenesis.ui.components.EvidenceBadge
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryControlCenterScreen(
    memories: List<MemoryRecord>,
    selectedFilterClass: String?,
    searchQuery: String,
    onFilterChange: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onApproveMemory: (String) -> Unit,
    onRejectMemory: (String) -> Unit,
    onDeleteMemory: (String) -> Unit,
    onSaveMemory: (MemoryRecord) -> Unit,
    onCorrectMemory: (String, String) -> Unit = { _, _ -> }
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<MemoryRecord?>(null) }
    val horizontalScroll = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "মেমোরি কন্ট্রোল সেন্টার",
            subtitle = "৯টি কাঠামোগত মেমোরি শ্রেণি"
        )

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "স্মৃতি বা উৎস অনুসন্ধান...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GenesisCyanAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Memory Classes Horizontal Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "সকল (${memories.size})",
                isSelected = selectedFilterClass == null,
                onClick = { onFilterChange(null) }
            )

            MemoryClass.entries.forEach { cls ->
                FilterChip(
                    label = cls.displayNameBn,
                    isSelected = selectedFilterClass == cls.name,
                    onClick = { onFilterChange(cls.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Memory List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (memories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোনো মেমোরি রেকর্ড পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(memories, key = { it.id }) { memory ->
                        MemoryItemCard(
                            record = memory,
                            onEdit = { editingMemory = memory },
                            onApprove = { onApproveMemory(memory.id) },
                            onReject = { onRejectMemory(memory.id) },
                            onDelete = { onDeleteMemory(memory.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // FAB to add memory manually
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fab_add_memory"),
                containerColor = GenesisCyanAccent,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "নতুন মেমোরি যোগ করুন")
            }
        }
    }

    // Dialog for Creating or Editing Memory
    if (showCreateDialog || editingMemory != null) {
        val target = editingMemory
        MemoryEditDialog(
            initialRecord = target,
            onDismiss = {
                showCreateDialog = false
                editingMemory = null
            },
            onSave = { record ->
                onSaveMemory(record)
                showCreateDialog = false
                editingMemory = null
                Toast.makeText(context, "মেমোরি সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GenesisCyanAccent else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) GenesisCyanAccent else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MemoryItemCard(
    record: MemoryRecord,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Class & Evidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GenesisIndigoDeep
                ) {
                    Text(
                        text = record.memoryClass.displayNameBn,
                        style = MaterialTheme.typography.labelSmall,
                        color = GenesisCyanAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    EvidenceBadge(level = record.evidenceLevel)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (record.status) {
                            MemoryStatus.CANDIDATE -> GenesisWarning.copy(alpha = 0.2f)
                            MemoryStatus.VERIFIED, MemoryStatus.ACTIVE -> GenesisSuccess.copy(alpha = 0.2f)
                            else -> Color.Gray.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = when (record.status) {
                                MemoryStatus.CANDIDATE -> "পরীক্ষাধীন"
                                MemoryStatus.VERIFIED -> "যাচাইকৃত"
                                MemoryStatus.ACTIVE -> "সক্রিয়"
                                MemoryStatus.INCORRECT -> "ভুল হিসেবে চিহ্নিত"
                                MemoryStatus.ARCHIVED -> "সংরক্ষিত"
                                else -> record.status.name
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (record.status) {
                                MemoryStatus.CANDIDATE -> GenesisWarning
                                MemoryStatus.VERIFIED, MemoryStatus.ACTIVE -> GenesisSuccess
                                else -> Color.Gray
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Text(
                text = record.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Source & Confidence
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "উৎস: ${record.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "আস্থা স্কোর: ${(record.confidenceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = GenesisCyanAccent,
                    fontSize = 11.sp
                )
            }

            // Expandable Why Retrieved & Metadata Details
            if (record.retrievalReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showDetails = !showDetails }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showDetails) "মেটাডাটা সংক্ষেপ লুকান" else "কেন এই মেমোরি সংরক্ষিত/আহরিত হয়েছে?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }

                AnimatedVisibility(visible = showDetails) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(GenesisIndigoDeep, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "আহরণের কারণ: ${record.retrievalReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "তৈরির সময়: ${dateFormat.format(Date(record.createdAt))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "সংবেদনশীলতা: ${record.sensitivityLevel.name} | আইডি: ${record.id.take(8)}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.status == MemoryStatus.CANDIDATE) {
                    OutlinedButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GenesisSuccess),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "অনুমোদন", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GenesisWarning),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "প্রত্যাখ্যান", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (record.isUserEditable) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }

                if (record.isDeletable) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryEditDialog(
    initialRecord: MemoryRecord?,
    onDismiss: () -> Unit,
    onSave: (MemoryRecord) -> Unit
) {
    var content by remember { mutableStateOf(initialRecord?.content ?: "") }
    var selectedClass by remember { mutableStateOf(initialRecord?.memoryClass ?: MemoryClass.PERMANENT_PREFERENCES) }
    var source by remember { mutableStateOf(initialRecord?.source ?: "ব্যবহারকারীর প্রত্যক্ষ এন্ট্রি") }
    var retrievalReason by remember { mutableStateOf(initialRecord?.retrievalReason ?: "ব্যবহারকারীর অনুমোদিত স্থায়ী মেমোরি।") }
    var classMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRecord == null) "নতুন মেমোরি যোগ করুন" else "মেমোরি সম্পাদনা",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("মেমোরির বিষয়বস্তু") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = classMenuExpanded,
                    onExpandedChange = { classMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClass.displayNameBn,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("মেমোরি শ্রেণি (৯টি)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classMenuExpanded,
                        onDismissRequest = { classMenuExpanded = false }
                    ) {
                        MemoryClass.entries.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls.displayNameBn) },
                                onClick = {
                                    selectedClass = cls
                                    classMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("তথ্যের উৎস") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = retrievalReason,
                    onValueChange = { retrievalReason = it },
                    label = { Text("আহরণের কারণ বা উদ্দেশ্য") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        val record = initialRecord?.copy(
                            content = content,
                            memoryClass = selectedClass,
                            source = source,
                            retrievalReason = retrievalReason,
                            lastVerifiedAt = System.currentTimeMillis()
                        ) ?: MemoryRecord(
                            id = UUID.randomUUID().toString(),
                            content = content,
                            memoryClass = selectedClass,
                            source = source,
                            retrievalReason = retrievalReason,
                            status = MemoryStatus.VERIFIED
                        )
                        onSave(record)
                    }
                },
                enabled = content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GenesisCyanAccent, contentColor = MaterialTheme.colorScheme.background)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
