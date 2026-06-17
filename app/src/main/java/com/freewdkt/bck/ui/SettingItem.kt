package com.freewdkt.bck.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//private val ButtonShape = RoundedCornerShape(16.dp)
@Composable
fun SettingsItem(
    icon: Painter,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,   // 可自定义图标颜色，默认使用主题主色
    cardColor: Color = MaterialTheme.colorScheme.surfaceVariant // 卡片背景动态取色
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标：支持动态取色（可跟随主题或手动指定）
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconTint   // 动态取色
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface  // 文字颜色自动适配主题
                )
              /*  Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )*/
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                //painter = painterResource(),
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "进入详情",
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
    }
}