package com.freewdkt.bck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.requestconstants.ApiConstants.userIcon
import com.freewdkt.bck.requestconstants.PrivateApi
import com.freewdkt.bck.ui.FreewdThemeColorCompose
import com.freewdkt.bck.ui.SettingsItem
import com.freewdkt.bck.ui.theme.FreewdKTTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FreewdKTTheme { UserCenterLayout() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCenterLayout() {
    val cardColor: Color = MaterialTheme.colorScheme.surfaceVariant
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // 签到状态：检查今天是否已经签过到
    val prefs = context.getSharedPreferences("freewd_checkin", Context.MODE_PRIVATE)
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    var isCheckedInToday by remember { mutableStateOf(prefs.getString("last_checkin_date", "") == today) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_center)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(start = 15.dp, end = 15.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column() {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 头像区域
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            AndroidView(
                                factory = { context ->
                                    ImageView(context).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                update = { imageView ->
                                    Glide.with(imageView.context)
                                        .load(
                                            userIcon(
                                                MyApplication.sessionManager.getQq().toString()
                                            )
                                        )
                                        .placeholder(R.mipmap.icon)
                                        .error(R.mipmap.icon)
                                        .into(imageView)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // 用户信息
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = MyApplication.sessionManager.getUsername() ?: "",
                                color = FreewdThemeColorCompose,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.mipmap.icon_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Freewd UID: ${MyApplication.sessionManager.getUid()}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Card(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        SettingsItem(
                            painterResource(R.drawable.mdicalendarcheck),
                            title = if (isCheckedInToday) {
                                "今日已签到（${MyApplication.sessionManager.getCheckInDays()}天）"
                            } else {
                                stringResource(R.string.checked).replace(
                                    "?%",
                                    MyApplication.sessionManager.getCheckInDays().toString()
                                )
                            },
                            description = if (isCheckedInToday) "明天再来吧～" else "",
                            onClick = {
                                if (isCheckedInToday) {
                                    Toast.makeText(context, "今天已经签到啦，明天再来吧～", Toast.LENGTH_SHORT).show()
                                    return@SettingsItem
                                }
                                scope.launch {
                                    isLoading = true
                                    try {
                                        val token = MyApplication.sessionManager.getToken()
                                        if (token.isNullOrBlank()) {
                                            Toast.makeText(
                                                context,
                                                "请先登录",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@launch
                                        }
                                        val days = checkInRequestSuspend(context, token)
                                        if (days.isNotEmpty()) {
                                            Log.d("woShiDAYS", days)
                                            MyApplication.sessionManager.updateCheckInDays(days.toIntOrNull() ?: 0)
                                            // 保存签到日期到本地
                                            prefs.edit().putString("last_checkin_date", today).apply()
                                            isCheckedInToday = true
                                            Toast.makeText(context, "签到成功！连续签到 $days 天", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                R.string.load_fail,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "签到失败", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Text(
                            stringResource(R.string.about),
                            modifier = Modifier.padding(start = 6.dp),
                            fontSize = 12.sp
                        )
                        SettingsItem(
                            painterResource(R.drawable.about),
                            stringResource(R.string.about_freewd),
                            stringResource(R.string.about_freewd),
                            onClick = {
                                val intent = Intent(context, AboutActivity::class.java).apply {
                                    putExtra("loadUrl", ApiConstants.UPDATE_LOG_URL)
                                }
                                (context as? Activity)?.startActivity(intent)
                            })
                        SettingsItem(
                            painterResource(R.drawable.agreement),
                            stringResource(R.string.user_agreement),
                            stringResource(R.string.user_agreement),
                            onClick = {
                                val intent = Intent(context, AboutActivity::class.java).apply {
                                    putExtra("loadUrl", ApiConstants.USER_AGREEMENT_URL)
                                }
                                (context as? Activity)?.startActivity(intent)
                            })
                        SettingsItem(
                            painterResource(R.drawable.agreement),
                            stringResource(R.string.privacy_policy),
                            stringResource(R.string.privacy_policy),
                            onClick = {
                                val intent = Intent(context, AboutActivity::class.java).apply {
                                    putExtra("loadUrl", ApiConstants.PRIVACY_POLICY_URL)
                                }
                                (context as? Activity)?.startActivity(intent)
                            })
                    }
                }
            }
        }
    }

    // 加载对话框
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { }, // 禁止点击外部关闭
            confirmButton = {},
            dismissButton = {},
            title = {},
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "签到中，请稍候...")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}

suspend fun checkInRequestSuspend(context: Context, token: String?): String {
    if (token.isNullOrBlank()) {
        return ""
    }
    val json = "{}"
    val body = json.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(PrivateApi.CHECKIN_URL)
        .addHeader("Authorization", "Bearer $token")
        .post(body)
        .build()
    val client = OkHttpClient()

    return suspendCancellableCoroutine { continuation ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, R.string.load_fail, Toast.LENGTH_SHORT).show()
                }
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, R.string.load_fail, Toast.LENGTH_SHORT).show()
                        }
                        continuation.resumeWithException(IOException("HTTP ${response.code}"))
                        return
                    }
                    val jsonString = response.body?.string() ?: ""
                    try {
                        val obj = JSONObject(jsonString)
                        val status = obj.getString("status")
                        if (status == "success") {
                            val checkinDays = obj.getString("checkin_days")
                            continuation.resume(checkinDays)
                        } else {
                            continuation.resume("")
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        })
    }
}