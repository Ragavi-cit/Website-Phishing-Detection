package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PriorityHigh
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttackStep
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.ThreatRed
import com.example.ui.theme.WarningAmber

@Composable
fun AttackReconstructionView(
    attackSteps: List<AttackStep>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    NeonCyan.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Attack Reconstruction",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "How This Attack Could Work",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Step-by-step threat trajectory & attacker intent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            attackSteps.forEachIndexed { index, step ->
                val isLast = index == attackSteps.size - 1
                AttackStepRow(step = step, isLast = isLast)
            }
        }
    }
}

@Composable
private fun AttackStepRow(
    step: AttackStep,
    isLast: Boolean
) {
    val icon = getStepIcon(step.iconType)
    val stepColor = getStepColor(step.iconType, step.stepNumber)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline node column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(stepColor.copy(alpha = 0.15f))
                    .border(1.5.dp, stepColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = step.title,
                    tint = stepColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    stepColor.copy(alpha = 0.6f),
                                    stepColor.copy(alpha = 0.15f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STAGE ${step.stepNumber}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = stepColor,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

private fun getStepIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "MESSAGE" -> Icons.Default.Email
        "URGENCY" -> Icons.Default.PriorityHigh
        "LINK" -> Icons.Default.Link
        "IMPERSONATION" -> Icons.Default.Warning
        "LOGIN_FORM" -> Icons.Default.LockOpen
        "CREDENTIAL_HARVEST" -> Icons.Default.Key
        "TAKEOVER" -> Icons.Default.Dangerous
        else -> Icons.Default.Security
    }
}

private fun getStepColor(type: String, stepNumber: Int): Color {
    return when (type.uppercase()) {
        "MESSAGE" -> Color(0xFF60A5FA)
        "URGENCY" -> WarningAmber
        "LINK" -> NeonCyan
        "IMPERSONATION" -> Color(0xFFF97316)
        "LOGIN_FORM" -> Color(0xFFFB7185)
        "CREDENTIAL_HARVEST" -> ThreatRed
        "TAKEOVER" -> ThreatRed
        else -> if (stepNumber <= 2) NeonCyan else if (stepNumber <= 4) WarningAmber else ThreatRed
    }
}
