package com.example.simplebutton.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.simplebutton.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.UserProfile
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import com.example.simplebutton.ui.theme.getHouseColor
import com.example.simplebutton.ui.theme.getHouseContentColor

@Composable
fun WelcomeOnboardingScreen(
    initialProfile: UserProfile = UserProfile(),
    onFinishOnboarding: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()

    var name by remember { mutableStateOf(initialProfile.name) }
    var selectedYear by remember { mutableStateOf(if (initialProfile.year.isBlank()) "S1" else initialProfile.year) }
    var selectedHouse by remember { mutableStateOf(if (initialProfile.house.isBlank() || initialProfile.house == "Custom") "Devon" else initialProfile.house) }
    var className by remember { mutableStateOf(initialProfile.className) }

    var isStartingIntroPlaying by remember { mutableStateOf(true) }
    var isWelcomeTransitionPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2200)
        isStartingIntroPlaying = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "startingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val isS1orS2 = selectedYear == "S1" || selectedYear == "S2"

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07162C),
            Color(0xFF0C2448),
            Color(0xFF15386B)
        )
    )

    // Handle the smooth finish trigger after the welcome transition
    fun triggerFinish() {
        val profile = UserProfile(
            name = name.trim(),
            year = selectedYear,
            house = selectedHouse,
            className = if (isS1orS2) className.trim() else "",
            completedOnboarding = true
        )
        onFinishOnboarding(profile)
    }

    LaunchedEffect(isWelcomeTransitionPlaying) {
        if (isWelcomeTransitionPlaying) {
            delay(1500)
            triggerFinish()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF07162C)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 1. Cinematic Starting Intro Phase
            AnimatedVisibility(
                visible = isStartingIntroPlaying && !isWelcomeTransitionPlaying,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { isStartingIntroPlaying = false }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Pulsing outer aura ring
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF38BDF8).copy(alpha = glowAlpha),
                                            Color(0xFF1D4ED8).copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Emblem
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF38BDF8))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF1B4985), Color(0xFF2C6ECB), Color(0xFF38BDF8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_lornshill_logo),
                                    contentDescription = "Lornshill Academy",
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "LORNSHILL ACADEMY",
                            fontFamily = Staatliches,
                            fontSize = 32.sp,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "TIMETABLE COMPANION",
                            fontFamily = Staatliches,
                            fontSize = 16.sp,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Your school day, organised.",
                            fontFamily = Lexend,
                            fontSize = 13.5.sp,
                            color = Color(0xFFBAE6FD),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        // Animated starting pill indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .clickable { isStartingIntroPlaying = false }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Starting companion...",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = Color(0xFFBAE6FD),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 2. Personalisation & Setup Form Phase
            AnimatedVisibility(
                visible = !isStartingIntroPlaying && !isWelcomeTransitionPlaying,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
                exit = fadeOut(tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    // School Crest / Timetable Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0xFF38BDF8))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1B4985), Color(0xFF2C6ECB), Color(0xFF38BDF8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lornshill_logo),
                            contentDescription = "Lornshill Academy Crest",
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "WELCOME TO LORNSHILL",
                        fontFamily = Staatliches,
                        fontSize = 28.sp,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Personalise your school companion & daily timetable",
                        fontFamily = Lexend,
                        fontSize = 13.5.sp,
                        color = Color(0xFFBAE6FD),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Setup Card with theme-aware background & proper contrast
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.cardBg
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Text(
                                text = "GETTING STARTED",
                                fontFamily = Staatliches,
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. Name Input (Theme-aware text & placeholder colours)
                            Text(
                                text = "Your Name (Optional)",
                                fontFamily = Lexend,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = {
                                    Text(
                                        "e.g. Alex",
                                        fontFamily = Lexend,
                                        fontSize = 14.sp,
                                        color = colors.textMuted
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedPlaceholderColor = colors.textMuted,
                                    unfocusedPlaceholderColor = colors.textMuted,
                                    focusedContainerColor = if (colors.isDark) Color(0xFF071731) else Color(0xFFF8FAFC),
                                    unfocusedContainerColor = if (colors.isDark) Color(0xFF071731) else Color(0xFFF8FAFC),
                                    focusedBorderColor = colors.accentCobalt,
                                    unfocusedBorderColor = colors.cardBorder,
                                    cursorColor = colors.accentCobalt
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Year Group Selector (All S1-S6 in a single evenly spaced row)
                            Text(
                                text = "Year Group",
                                fontFamily = Lexend,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                UserProfile.YEARS.forEach { yr ->
                                    val isSelected = selectedYear == yr
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) LornshillCobalt else colors.chipBg
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) LornshillCobalt else colors.cardBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedYear = yr }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = yr,
                                            fontFamily = Lexend,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3. House Selector (4 Authentic Houses: Devon, Forebraes, Grange, Ochil - No Custom, 2x2 grid)
                            Text(
                                text = "House",
                                fontFamily = Lexend,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val houseList = listOf("Devon", "Forebraes", "Grange", "Ochil")
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (row in houseList.chunked(2)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        row.forEach { houseName ->
                                            val isSelected = selectedHouse == houseName
                                            val houseColor = getHouseColor(houseName)
                                            val houseContentColor = getHouseContentColor(houseName)

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) houseColor else colors.chipBg
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 1.dp,
                                                        color = if (isSelected) houseColor else colors.cardBorder,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedHouse = houseName }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    if (isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(houseContentColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = houseName,
                                                        fontFamily = Lexend,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) houseContentColor else colors.textPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Class Name (CONDITIONAL: only if S1 or S2, simply "Class Name (Optional)")
                            AnimatedVisibility(
                                visible = isS1orS2,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Class Name (Optional)",
                                        fontFamily = Lexend,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = className,
                                        onValueChange = { className = it },
                                        placeholder = {
                                            Text(
                                                "e.g. 1C1, 1G2, 2D1",
                                                fontFamily = Lexend,
                                                fontSize = 14.sp,
                                                color = colors.textMuted
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedPlaceholderColor = colors.textMuted,
                                            unfocusedPlaceholderColor = colors.textMuted,
                                            focusedContainerColor = if (colors.isDark) Color(0xFF071731) else Color(0xFFF8FAFC),
                                            unfocusedContainerColor = if (colors.isDark) Color(0xFF071731) else Color(0xFFF8FAFC),
                                            focusedBorderColor = colors.accentCobalt,
                                            unfocusedBorderColor = colors.cardBorder,
                                            cursorColor = colors.accentCobalt
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Settings Notice (Theme-aware)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (colors.isDark) Color(0xFF0C254A) else LornshillBlueContainer.copy(alpha = 0.6f))
                                    .border(1.dp, if (colors.isDark) Color(0xFF1B3D6D) else Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colors.accentCobalt,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "You can change your name, year, house, and class anytime in Settings.",
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            // Begin Action Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LornshillButtonGradient)
                                    .clickable {
                                        isWelcomeTransitionPlaying = true
                                    }
                                    .padding(vertical = 15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Begin",
                                        fontFamily = Lexend,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // 3. Welcome Interstitial Transition Phase ("Welcome (name)")
            AnimatedVisibility(
                visible = isWelcomeTransitionPlaying,
                enter = fadeIn(tween(400)) + scaleIn(tween(500), initialScale = 0.9f),
                exit = fadeOut(tween(400)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { triggerFinish() }
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Pulsing Crest
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .shadow(20.dp, CircleShape, spotColor = Color(0xFF38BDF8))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1B4985), Color(0xFF2C6ECB), Color(0xFF38BDF8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_lornshill_logo),
                                contentDescription = "Lornshill Academy",
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val welcomeName = if (name.isNotBlank()) name.trim().uppercase() else "STUDENT"
                        Text(
                            text = "WELCOME, $welcomeName!",
                            fontFamily = Staatliches,
                            fontSize = 30.sp,
                            color = Color.White,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$selectedYear · $selectedHouse House",
                            fontFamily = Lexend,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Your school companion is ready.\nLet's take a quick look around your timetable.",
                            fontFamily = Lexend,
                            fontSize = 13.5.sp,
                            color = Color(0xFFBAE6FD),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { triggerFinish() }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Let's Go",
                                    fontFamily = Lexend,
                                    fontSize = 13.5.sp,
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
        }
    }
}
