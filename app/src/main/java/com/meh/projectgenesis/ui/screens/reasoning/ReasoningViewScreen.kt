package com.meh.projectgenesis.ui.screens.reasoning

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meh.projectgenesis.domain.model.ReasoningPipelineResult
import com.meh.projectgenesis.ui.components.AutonomyBadge
import com.meh.projectgenesis.ui.components.EvidenceBadge
import com.meh.projectgenesis.ui.components.GenesisTopBar
import com.meh.projectgenesis.ui.theme.GenesisCyanAccent
import com.meh.projectgenesis.ui.theme.GenesisIndigoDeep
import com.meh.projectgenesis.ui.theme.GenesisSuccess
import com.meh.projectgenesis.ui.theme.GenesisWarning

@Composable
fun ReasoningViewScreen(
    result: ReasoningPipelineResult?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GenesisTopBar(
            title = "রিজনিং পাইপলাইন ভিউ",
            subtitle = "১১-ধাপের অভ্যন্তরীণ যুক্তি ডায়াগনস্টিকস",
            onBack = onBack
        )

        if (result == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = GenesisCyanAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "কোনো সাম্প্রতিক পাইপলাইন রান নেই",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "কথোপকথন স্ক্রিনে কোনো প্রশ্ন পাঠালে তার অভ্যন্তরীণ যুক্তিবাদী ধাপসমূহ এখানে বিস্তারিত দেখা যাবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Step 1: Intake Card
                ReasoningStepCard(
                    stepNumber = "১",
                    stepTitle = "ইনটেক ও উদ্দেশ্য অনুধাবন",
                    icon = Icons.Default.HelpOutline,
                    accentColor = GenesisCyanAccent
                ) {
                    Text(
                        text = result.interpretedRequest,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ভাষা সনাক্তকরণ: ${if (result.language == "bn") "বাংলা (সম্বোধন: আপনি)" else "English"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GenesisCyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 2 & 3: Context Retrieval & Isolation Check
                ReasoningStepCard(
                    stepNumber = "২-৩",
                    stepTitle = "প্রসঙ্গ আহরণ ও প্রজেক্ট আইসোলেশন",
                    icon = Icons.Default.FactCheck,
                    accentColor = Color(0xFF00B0FF)
                ) {
                    if (result.relevantContextSummaries.isEmpty()) {
                        Text(
                            text = "কোনো প্রাসঙ্গিক মেমোরি রেকর্ডের প্রয়োজন হয়নি (মুক্ত সংলাপ)।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "আহরিত মেমোরি রেকর্ডসমূহ (${result.relevantContextSummaries.size}টি):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        result.relevantContextSummaries.forEach { summary ->
                            Text(
                                text = "• $summary",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ প্রজেক্ট আইসোলেশন পলিসি: অন্য প্রজেক্টের সাথে ডেটা মিশ্রণ রোধ করা হয়েছে।",
                        style = MaterialTheme.typography.labelSmall,
                        color = GenesisSuccess
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 4: Claim Separation
                ReasoningStepCard(
                    stepNumber = "৪",
                    stepTitle = "দাবি পৃথকীকরণ (Claim Separation)",
                    icon = Icons.Default.FactCheck,
                    accentColor = Color(0xFF7C4DFF)
                ) {
                    result.claimsSeparated.forEach { claim ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            EvidenceBadge(level = claim.evidenceLevel)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${claim.type}: ${claim.text}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 5: Contradiction Check
                ReasoningStepCard(
                    stepNumber = "৫",
                    stepTitle = "স্ববিরোধিতা ও যুক্তিদোষ নিরীক্ষা",
                    icon = Icons.Default.Warning,
                    accentColor = if (result.contradictions.isEmpty()) GenesisSuccess else GenesisWarning
                ) {
                    if (result.contradictions.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GenesisSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কোনো যৌক্তিক স্ববিরোধ বা অযাচাইকৃত দাবি সনাক্ত হয়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = GenesisSuccess
                            )
                        }
                    } else {
                        result.contradictions.forEach { item ->
                            Text(
                                text = "⚠️ ${item.issue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GenesisWarning,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 6 & 7: Evidence & Autonomy Check
                ReasoningStepCard(
                    stepNumber = "৬-৭",
                    stepTitle = "প্রমাণ ও অনুমতি স্তর যাচাই",
                    icon = Icons.Default.Security,
                    accentColor = GenesisCyanAccent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "সর্বোচ্চ প্রমাণ স্তর:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            EvidenceBadge(level = result.highestEvidenceLevel)
                        }
                        Column {
                            Text(text = "প্রয়োজনীয় অনুমতি স্তর:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            AutonomyBadge(level = result.requiredAutonomy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 9: Internal Critic Review
                ReasoningStepCard(
                    stepNumber = "৯",
                    stepTitle = "অভ্যন্তরীণ সমালোচক পর্যালোচনা (Critic Review)",
                    icon = Icons.Default.AssignmentTurnedIn,
                    accentColor = GenesisSuccess
                ) {
                    val critic = result.criticReview
                    Text(text = "• দুর্বল অনুমান উপস্থিতি: ${if (critic.hasWeakAssumptions) "হ্যাঁ (চিহ্নিত)" else "নেই"}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• অনুপস্থিত প্রমাণ যাচাই: ${if (critic.missingEvidenceDetected) "সতর্কতা জারি" else "যাচাইকৃত"}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• অতি-আত্মবিশ্বাস সংশোধন: ${if (critic.overconfidenceCorrected) "সংশোধিত" else "স্বাভাবিক"}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• নিরাপত্তা ও সম্মতি: ${if (critic.safeForExecution) "অনুমোদিত ও নিরাপদ" else "স্থগিত"}", style = MaterialTheme.typography.bodySmall, color = if (critic.safeForExecution) GenesisSuccess else GenesisWarning)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 10: Final Synthesis Output
                ReasoningStepCard(
                    stepNumber = "১০-১১",
                    stepTitle = "চূড়ান্ত সিদ্ধান্ত ও লার্নিং রিভিউ",
                    icon = Icons.Default.CheckCircle,
                    accentColor = GenesisCyanAccent
                ) {
                    Text(
                        text = result.decisionExplanationBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (result.candidateLearning != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 প্রার্থী লার্নিং মেমোরি সনাক্ত হয়েছে (লার্নিং রিভিউতে অনুমোদনের জন্য অপেক্ষমাণ)।",
                            style = MaterialTheme.typography.labelSmall,
                            color = GenesisCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReasoningStepCard(
    stepNumber: String,
    stepTitle: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
