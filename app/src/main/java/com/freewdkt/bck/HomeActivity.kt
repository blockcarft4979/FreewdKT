package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeActivityBinding
    private lateinit var zoneAdapter: ZoneAdapter

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

        // 处理边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化适配器
        zoneAdapter = ZoneAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = zoneAdapter

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
        //binding.userIcon.load(userIcon)

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
                }else if (isNetworkError){Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show()}
            }
        }

        // 首次加载板块数据
        loadZones()

        // 板块点击事件
        zoneAdapter.onItemClick = { zone ->
            if (zone.link != null) {
                val intent = Intent(this, FreewdBrowser::class.java).apply {
                    putExtra("link", zone.link)
                }
                startActivity(intent)
            }
            if (zone.zone != null) {
                val intent = Intent(this, PostList::class.java).apply {
                    putExtra("zoneId", zone.zone.toString())
                }
                startActivity(intent)
            }
            if (zone.msg != null) {
                Toast.makeText(this, zone.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }


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
                        .setAction(getString(R.string.retry)) {
                            loadZones()
                        }
                        .show()
                } else {
                    zoneList?.let {
                        zoneAdapter.submitList(it)
                    }
                }
            }
        }
    }
}