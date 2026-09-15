package com.example.simplebutton.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillLightBlue
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

enum class TourStep(val stepNumber: Int, val totalSteps: Int) {
    VIEW_FULL(1, 3),
    DAYS(2, 3),
    PERIODS(3, 3)
}

@Composable
fun OnboardingTourOverlay(
    currentStep: TourStep,
    studentName: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val infiniteTransition = rememberInfiniteTransition(label = "spotlightHalo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    val displayName = if (studentName.isNotBlank()) studentName.trim() else "there"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030A16).copy(alpha = 0.75f))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Consume taps outside */ }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar with Step Dots and Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step indicator pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val isActive = currentStep.stepNumber == i
                    val isDone = currentStep.stepNumber > i
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) LornshillLightBlue
                                else if (isDone) Color.White.copy(alpha = 0.8f)
                                else Color.White.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            // Skip button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable { onSkip() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Skip Tour",
                        fontFamily = Lexend,
                        fontSize = 11.5.sp,
                        color = Color(0xFFBAE6FD),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tour",
                        tint = Color(0xFFBAE6FD),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Spotlight Element & Floating Tooltip Card
        when (currentStep) {
            TourStep.VIEW_FULL -> {
                // Step 1: Spotlights the "View full ->" button at the top right of the schedule section on HomeScreen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing spotlight preview tag
                    Box(
                        modifier = Modifier
                            .scale(haloPulse)
                            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF38BDF8))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1D4ED8).copy(alpha = haloAlpha),
                                        Color(0xFF38BDF8).copy(alpha = haloAlpha)
                                    )
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF071B38))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "View full →",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Floating explanation card
                    TourTooltipCard(
                        eyebrow = "WELCOME, ${displayName.uppercase()}!",
                        title = "Your Full Timetable",
                        description = "Tap 'View full' anytime on your home screen to open your complete Monday to Friday schedule.",
                        actionLabel = "Show Me →",
                        stepText = "Step 1 of 3",
                        onAction = onNext
                    )
                }
            }

            TourStep.DAYS -> {
                // Step 2: Spotlights the Day Selector (Mon - Fri) on TimetableScreen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Highlight representation of the Day Selector
                    Box(
                        modifier = Modifier
                            .scale(haloPulse)
                            .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF38BDF8))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1D4ED8).copy(alpha = haloAlpha),
                                        Color(0xFF38BDF8).copy(alpha = haloAlpha)
                                    )
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF071B38))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEachIndexed { idx, day ->
                                    val isSelected = idx == 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) LornshillCobalt else Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = day,
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TourTooltipCard(
                        eyebrow = "DAYS OF THE WEEK",
                        title = "Switch Between Days",
                        description = "Tap any day (Monday to Friday) to view its scheduled periods, morning interval, and lunch times.",
                        actionLabel = "Next: Add Periods →",
                        stepText = "Step 2 of 3",
                        onAction = onNext
                    )
                }
            }

            TourStep.PERIODS -> {
                // Step 3: Spotlights the Periods list and explains how to add/edit classes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Highlight representation of a Period Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .scale(haloPulse)
                            .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF38BDF8))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1D4ED8).copy(alpha = haloAlpha),
                                        Color(0xFF38BDF8).copy(alpha = haloAlpha)
                                    )
                                )
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF071B38))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(LornshillCobalt.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "P1",
                                            fontFamily = Staatliches,
                                            fontSize = 15.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Tap to add subject...",
                                            fontFamily = Lexend,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Teacher name",
                                            fontFamily = Lexend,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+ Tap",
                                        fontFamily = Lexend,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TourTooltipCard(
                        eyebrow = "SETTING UP CLASSES",
                        title = "Tap Any Period to Add",
                        description = "Just tap any period to choose your subject and teacher name. It only takes a minute to set up your week!",
                        actionLabel = "Ready to Start! ✨",
                        stepText = "Step 3 of 3",
                        onAction = onNext
                    )
                }
            }
        }
    }
}

@Composable
private fun TourTooltipCard(
    eyebrow: String,
    title: String,
    description: String,
    actionLabel: String,
    stepText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B1F3B)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B3D6D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = eyebrow,
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                )
                Text(
                    text = stepText,
                    fontFamily = Lexend,
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontFamily = Lexend,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontFamily = Lexend,
                fontSize = 13.5.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LornshillButtonGradient)
                    .clickable { onAction() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = actionLabel,
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
