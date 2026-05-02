package com.freewdkt.bck

import com.freewdkt.bck.data.LoginRequest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.freewdkt.bck.databinding.ActivityMainBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        var intent = Intent(this, HomeActivity::class.java)
//        startActivity(intent)
//        finish()
//        return

        if (MyApplication.sessionManager.isLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 初始化加载对话框
        val dialogLoading = layoutInflater.inflate(R.layout.dialog_loading, null)
        loadingDialog =
            MaterialAlertDialogBuilder(this).setView(dialogLoading).setCancelable(false).create()

        // 处理边距


        // 监听账号输入框，实时显示头像
        binding.accountEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                if (input.isNotEmpty()) {
                    Glide.with(binding.iconImage)
                        .load(ApiConstants.userIcon(input))
                        .into(binding.iconImage)

                } else {
                    binding.iconImage.setImageResource(R.mipmap.icon)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 点击复选框跳转用户协议
        binding.agreeBox.setOnClickListener {
            if (binding.agreeBox.isChecked) {
                val intent = Intent(this, UserAgreementActivity::class.java)
                startActivity(intent)
            }
        }

        // 登录按钮点击事件
        binding.loginButton.setOnClickListener {
            if (binding.agreeBox.isChecked) {
                val account = binding.accountEdit.text.toString()
                val password = binding.passwordEdit.text.toString()
                loadingDialog.show()
                LoginRequest(
                    applicationContext, account, password
                ).execute { success, loginResponse, message ->
                    runOnUiThread {
                        loadingDialog.dismiss()
                        if (success && loginResponse != null) {
                            // 保存用户信息到加密存储
                            MyApplication.sessionManager.saveUserSession(
                                token = loginResponse.token ?: "",
                                uid = loginResponse.uid ?: 0,
                                username = loginResponse.username ?: "",
                                xp = loginResponse.xp ?: 0,
                                qq = binding.accountEdit.text.toString()
                            )
                            //Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            // 跳转到主页（示例）
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                message ?: getString(R.string.login_timeout),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.please_agree), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 登录请求封装类

}