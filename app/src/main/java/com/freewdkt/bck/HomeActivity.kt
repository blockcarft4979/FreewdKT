package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.freewdkt.bck.adapter.ZoneAdapter
import com.freewdkt.bck.data.RequestData
import com.freewdkt.bck.data.verifyToken
import com.freewdkt.bck.databinding.HomeActivityBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeActivityBinding
    private lateinit var zoneAdapter: ZoneAdapter
    private var unreadBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 检查是否登录
        if (!MyApplication.sessionManager.isLoggedIn()) {
            Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // 处理窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化适配器
        zoneAdapter = ZoneAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = zoneAdapter

        binding.userIcon.setOnClickListener {
            val intent = Intent(this, UserCenterActivity::class.java)
            startActivity(intent)
        }
        // 获取用户信息并更新 UI
        val userName = MyApplication.sessionManager.getUsername()
        val userUid = "${getString(R.string.freewd_uid)}${MyApplication.sessionManager.getUid()}"
        val userIcon = ApiConstants.userIcon(MyApplication.sessionManager.getQq() ?: "")
        val token = MyApplication.sessionManager.getToken() ?: ""

        binding.userUid.text = userUid
        binding.userName.text = userName
        Glide.with(binding.userIcon)
            .load(userIcon)
            .into(binding.userIcon)

        // 验证 Token
        verifyToken(this, token) { isValid, isNetworkError, username, msg ->
            runOnUiThread {
                if (!isValid && !isNetworkError) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.tips))
                        .setMessage(msg ?: getString(R.string.verify_failed))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            MyApplication.sessionManager.clear()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else if (isValid) {
                    username?.takeIf { it.isNotBlank() }?.let { serverUsername ->
                        val localUsername = MyApplication.sessionManager.getUsername()
                        if (serverUsername != localUsername) {
                            MyApplication.sessionManager.updateUsername(serverUsername)
                            binding.userName.text = serverUsername
                        }
                    }
                } else if (isNetworkError) {
                    Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        // 首次加载板块数据
        loadZones()

        // 板块点击事件
        zoneAdapter.onItemClick = { zone ->
            when {
                zone.link != null -> {
                    val intent = Intent(this, FreewdBrowser::class.java).apply {
                        putExtra("link", zone.link)
                    }
                    startActivity(intent)
                }

                zone.zone != null -> {
                    val intent = Intent(this, PostList::class.java).apply {
                        putExtra("zoneId", zone.zone.toString())
                    }
                    startActivity(intent)
                }

                zone.msg != null -> {
                    Toast.makeText(this, zone.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // messageIcon 点击跳转通知页

        binding.messageIcon.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // 初始化未读消息角标
        setupUnreadBadge()

    }

    // ==================== 未读消息角标 ====================

    @OptIn(ExperimentalBadgeUtils::class)
    private fun setupUnreadBadge() {
        unreadBadge = BadgeDrawable.create(this).apply {
            isVisible = false
            maxCharacterCount = 3
            badgeGravity = BadgeDrawable.TOP_END
        }

        unreadBadge?.let { badge ->
            BadgeUtils.attachBadgeDrawable(badge, binding.messageIcon, null)
        }

        // 首次获取未读数
        fetchUnreadCount()
    }

    private fun fetchUnreadCount() {
        val token = MyApplication.sessionManager.getToken() ?: run {
            Log.w("HomeActivity", "Token is null – cannot fetch unread count")
            return
        }
        val url = PrivateApi.UNREAD_COUNT_URL

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = "{}".toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeActivity", "fetchUnreadCount failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.w("HomeActivity", "Unsuccessful response: $response")
                    return
                }
                val json = response.body?.string()
                if (json != null) {
                    try {
                        val obj = JSONObject(json)
                        // 根据实际返回字段修改这里（默认使用 "unread"）
                        val unread = obj.optInt("unread", 0)
                        Log.d("HomeActivity", "unread from server = $unread")
                        // 调试时可硬编码一个数字确认 UI 正常
                        // runOnUiThread { updateBadge(7) }
                        runOnUiThread { updateBadge(unread) }
                    } catch (ex: Exception) {
                        Log.e("HomeActivity", "JSON parse error", ex)
                    }
                }
            }
        })
    }

    private fun updateBadge(count: Int) {
        unreadBadge?.apply {
            if (count > 0) {
                number = count
                isVisible = true
            } else {
                isVisible = false
            }
        }
    }

    // ==================== 区块数据 ====================

    private fun loadZones() {
        val requestData = RequestData(applicationContext)
        requestData.fetchZones { zoneList, error ->
            runOnUiThread {
                if (error != null) {
                    Snackbar.make(
                        binding.root,
                        "${getString(R.string.load_fail)}$error",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.retry)) { loadZones() }
                        .show()
                } else {
                    zoneList?.let { zoneAdapter.submitList(it) }
                }
            }
        }
    }
}
