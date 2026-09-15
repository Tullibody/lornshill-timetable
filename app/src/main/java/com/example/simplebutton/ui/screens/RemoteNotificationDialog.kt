package com.example.simplebutton.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.simplebutton.remote.RemoteAppNotification
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCardGradient
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

/**
 * In-app popup for displaying remote announcements/notices.
 * Fits the official Lornshill Timetable design aesthetic.
 * Automatically wraps long messages in a scrollable view that remains
 * comfortably within the device screen boundaries.
 */
@Composable
fun RemoteNotificationDialog(
    notification: RemoteAppNotification,
    onDismiss: () -> Unit
) {
    val colors = appColors()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            color = colors.cardBg,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Notice Emblem Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(LornshillCardGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📢",
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Official School Notice Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.accentCobalt.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "SCHOOL NOTICE",
                        fontFamily = Staatliches,
                        fontSize = 11.sp,
                        color = colors.accentCobalt,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = notification.title,
                    fontFamily = Staatliches,
                    fontSize = 21.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message Body: Scrollable with max height constraint so it adapts to long messages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.cardSecondaryBg)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = notification.message,
                        fontFamily = Lexend,
                        fontSize = 13.5.sp,
                        color = colors.textPrimary,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action & Dismiss Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Optional Action Button (if URL provided)
                    if (!notification.actionUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LornshillButtonGradient)
                                .clickable {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(notification.actionUrl.trim())
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Could not open link: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = notification.actionText?.ifBlank { "Learn More" } ?: "Learn More",
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

                    // Close / Dismiss Button
                    if (notification.actionUrl.isNullOrBlank()) {
                        // If no action button, Close button gets primary gradient styling
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LornshillButtonGradient)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = notification.buttonText.ifBlank { "Close" },
                                fontFamily = Lexend,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // If there is an action button, Close button is secondary
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = notification.buttonText.ifBlank { "Close" },
                                fontFamily = Lexend,
                                fontSize = 13.5.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
