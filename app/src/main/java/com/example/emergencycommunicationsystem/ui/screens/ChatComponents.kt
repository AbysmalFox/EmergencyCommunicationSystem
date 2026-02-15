package com.example.emergencycommunicationsystem.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import com.example.emergencycommunicationsystem.ui.icons.AppIcons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emergencycommunicationsystem.data.models.Message
import com.example.emergencycommunicationsystem.ui.theme.ChatHeaderTeal
import com.example.emergencycommunicationsystem.ui.theme.ChatIncomingBubbleLight
import com.example.emergencycommunicationsystem.ui.theme.ChatOutgoingBubbleLight
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            val isBot = message.senderId == "0" || message.senderName.contains("bot", ignoreCase = true)
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = ChatHeaderTeal.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isBot) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = ChatHeaderTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = message.senderName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = ChatHeaderTeal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 2.dp,
                        bottomEnd = if (isCurrentUser) 2.dp else 16.dp
                    ))
                    .background(
                        color = if (isCurrentUser) ChatOutgoingBubbleLight else ChatIncomingBubbleLight,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 2.dp,
                            bottomEnd = if (isCurrentUser) 2.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
                    .widthIn(max = 260.dp)
            ) {
                Column {
                    Text(
                        text = message.messageText,
                        color = if (isCurrentUser) Color.White else Color.Black.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }
            
            Text(
                text = formatTime(message.createdAt),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                color = ChatHeaderTeal
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatTime(timestamp: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val odt = if (timestamp.contains("T")) {
                java.time.OffsetDateTime.parse(timestamp)
            } else {
                java.time.LocalDateTime.parse(timestamp.replace(" ", "T")).atOffset(java.time.ZoneOffset.UTC)
            }
            odt.format(DateTimeFormatter.ofPattern("h:mm a"))
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(timestamp)
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            formatter.format(date!!)
        }
    } catch (e: Exception) {
        timestamp
    }
}

class CurvedHeaderShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.8f)
            quadraticBezierTo(size.width * 0.75f, size.height, size.width * 0.5f, size.height * 0.9f)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.8f, 0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class CurvedFooterShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, size.height * 0.2f)
            quadraticBezierTo(size.width * 0.75f, 0f, size.width * 0.5f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.25f, size.height * 0.3f, 0f, size.height * 0.1f)
            close()
        }
        return Outline.Generic(path)
    }
}
