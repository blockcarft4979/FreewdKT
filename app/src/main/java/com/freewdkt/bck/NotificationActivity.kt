package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.freewdkt.bck.data.NotificationItem
import com.freewdkt.bck.requestconstants.ApiConstants.userIcon
import com.freewdkt.bck.requestconstants.PrivateApi
import com.freewdkt.bck.ui.theme.FreewdKTTheme
import com.freewdkt.bck.utils.formatRelativeTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreewdKTTheme {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var uiState by remember { mutableStateOf<NotificationUiState>(NotificationUiState.Loading) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiState = NotificationUiState.Loading
        val notifications = fetchNotificationsFromServer()
        uiState = NotificationUiState.Success(notifications)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications)) },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "正在制作中！", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_clear_all_24),
                            contentDescription = stringResource(R.string.clear_all_notifications)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is NotificationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.loading_notifications))
                        }
                    }
                }

                is NotificationUiState.Success -> {
                    val notifications = (uiState as NotificationUiState.Success).notifications
                    if (notifications.isEmpty()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_notifications))
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(notifications, key = { it.createdAt }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        // 标记已读（本地更新）
                                        /* val updated = notifications.map {
                                             if (it.id == notification.id) it.copy(isRead = true) else it
                                         }*/
                                        //uiState = NotificationUiState.Success(updated)
                                        // 跳转
                                        val intent =
                                            Intent(context, PostDetails::class.java).apply {
                                                putExtra("filename", notification.link)
                                                putExtra("zone", notification.zone.toString())
                                                putExtra(
                                                    "url",
                                                    PrivateApi.postDetailUrl(
                                                        notification.link,
                                                        notification.zone.toString()
                                                    )
                                                )
                                            }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }

                is NotificationUiState.Error -> {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text((uiState as NotificationUiState.Error).message)
                            Button(onClick = {
                                // 重试：重新加载
                                uiState = NotificationUiState.Loading
                                // 重新调用加载逻辑（可以用协程）
                            }) {
                                Text("重试")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 头像（使用 Glide 加载）
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                update = { imageView ->
                    Glide.with(imageView.context)
                        .load(userIcon(notification.qq))
                        .placeholder(R.mipmap.icon)
                        .error(R.mipmap.icon)
                        .into(imageView)
                }
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 内容区域（不变）
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = buildString {
                            append(notification.username)
                            when (notification.replyType) {
                                "reply" -> append(" 回复了你")
                                "like" -> append(" 赞了你的帖子")
                                "system" -> append("")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (notification.isRead) MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        )
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                        text = formatRelativeTime(notification.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            // 未读小红点
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}


sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<NotificationItem>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

suspend fun fetchNotificationsFromServer(): List<NotificationItem> =
    withContext(Dispatchers.IO) {
        val token = MyApplication.sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            throw IOException("未登录")
        }

        val request = Request.Builder()
            .url(PrivateApi.NOTIFICATION_LIST_URL)
            .post("{}".toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }

        val json = response.body?.string()
        if (json.isNullOrEmpty()) throw IOException("空响应")
        val obj = JSONObject(json)
        if (obj.optString("status") != "success") {
            throw IOException(obj.optString("msg", "请求失败"))
        }

        val arr = obj.optJSONArray("notifications") ?: return@withContext emptyList()
        val list = mutableListOf<NotificationItem>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            list.add(
                NotificationItem(
                   // id = item.optString("link"),
                    link = item.optString("link"),
                    qq = item.optString("qq"),
                    username = item.optString("username"),
                    zone = item.optInt("zone"),
                    content = item.optString("content"),
                    replyType = item.optString("reply_type"),
                    createdAt = item.optString("created_at"),
                    isRead = item.optInt("is_read") == 1
                )
            )
        }
        return@withContext list
    }