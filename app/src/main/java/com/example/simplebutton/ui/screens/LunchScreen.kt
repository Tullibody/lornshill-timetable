package com.example.simplebutton.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillCardGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillHeaderGradient
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillSkyLight
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

@Composable
fun LunchScreen(
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBg)
    ) {
        // Gradient Header extending into Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillHeaderGradient)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = "LUNCH MENUS",
                    fontFamily = Staatliches,
                    fontSize = 20.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Lornshill Academy Dining Hub",
                    fontFamily = Lexend,
                    fontSize = 12.sp,
                    color = Color(0xFFBAE6FD)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Polished Coming Soon Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LornshillBlueContainer)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "COMING SOON",
                            fontFamily = Staatliches,
                            fontSize = 12.sp,
                            color = LornshillCobalt,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Illustrated Dining Badge with Blue Gradient
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(LornshillCardGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "LUNCH MENUS",
                        fontFamily = Staatliches,
                        fontSize = 24.sp,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We're working on bringing the latest school lunch information directly into the app.",
                        fontFamily = Lexend,
                        fontSize = 13.5.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Official Lunch Timings Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (colors.isDark) colors.cardSecondaryBg else Color(0xFFEFF6FF))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OFFICIAL DINING & BREAK TIMES",
                            fontFamily = Staatliches,
                            fontSize = 13.sp,
                            color = colors.accentCobalt,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mon, Tue, Thu, Fri Lunch", fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                Text("Interval: 10:45 — 11:00", fontFamily = Lexend, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Text("12:40 — 13:15", fontFamily = Lexend, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = colors.accentCobalt)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wednesday Lunch", fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                                Text("Interval: 10:35 — 10:50", fontFamily = Lexend, fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Text("12:30 — 13:00", fontFamily = Lexend, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = colors.accentCobalt)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Features Coming Preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (colors.isDark) colors.cardSecondaryBg else Color(0xFFF8FAFD))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureCheckItem("Daily specials & vegetarian meal options")
                        FeatureCheckItem("Allergen & nutritional breakdown")
                        FeatureCheckItem("Dining hall pre-orders & meal balances")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckItem(text: String) {
    val colors = appColors()
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.accentCobalt.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.accentCobalt,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = Lexend,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}
