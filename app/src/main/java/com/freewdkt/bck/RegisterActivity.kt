package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.freewdkt.bck.data.RegisterRequest
import com.freewdkt.bck.data.SendCodeRequest
import com.freewdkt.bck.databinding.ActivityRegisterBinding
import com.freewdkt.bck.requestconstants.ApiConstants

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 监听 QQ 输入，实时显示头像
        binding.registerQqEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                if (input.isNotEmpty()) {
                    Glide.with(binding.registerIconImage)
                        .load(ApiConstants.userIcon(input))
                        .into(binding.registerIconImage)
                } else {
                    binding.registerIconImage.setImageResource(R.mipmap.icon)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 发送验证码
        binding.sendCodeBtn.setOnClickListener {
            sendVerificationCode()
        }

        // 注册按钮
        binding.registerBtn.setOnClickListener {
            performRegister()
        }

        // 返回登录
        binding.backToLoginBtn.setOnClickListener {
            finish()
        }

        // 同意协议
        binding.registerAgreeBox.setOnClickListener {
            if (binding.registerAgreeBox.isChecked) {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    /**
     * 发送验证码
     */
    private fun sendVerificationCode() {
        val qq = binding.registerQqEdit.text.toString().trim()

        if (qq.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_password_account), Toast.LENGTH_SHORT).show()
            return
        }
        if (qq.length < 5 || qq.length > 12) {
            Toast.makeText(this, "QQ号格式不正确（5-12位）", Toast.LENGTH_SHORT).show()
            return
        }

        binding.sendCodeBtn.isEnabled = false
        binding.sendCodeBtn.text = getString(R.string.sending_code)

        SendCodeRequest(applicationContext).sendCode(qq) { success, msg ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "验证码已发送至 ${qq}@qq.com", Toast.LENGTH_SHORT).show()
                    startCountDown()
                } else {
                    binding.sendCodeBtn.isEnabled = true
                    binding.sendCodeBtn.text = getString(R.string.send_code)
                    val errorMsg = when {
                        msg?.contains("已注册") == true -> "该 QQ 号已被注册"
                        msg?.contains("邮件") == true -> "邮件发送失败，请稍后重试"
                        else -> msg ?: "发送失败"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 60秒倒计时
     */
    private fun startCountDown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.sendCodeBtn.text = "${seconds}s"
            }

            override fun onFinish() {
                binding.sendCodeBtn.isEnabled = true
                binding.sendCodeBtn.text = getString(R.string.send_code)
            }
        }.start()
    }

    /**
     * 执行注册
     */
    private fun performRegister() {
        val qq = binding.registerQqEdit.text.toString().trim()
        val password = binding.registerPasswordEdit.text.toString()
        val code = binding.registerCodeEdit.text.toString().trim()

        if (qq.isEmpty() || password.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_password_account), Toast.LENGTH_SHORT).show()
            return
        }
        if (qq.length < 5 || qq.length > 12) {
            Toast.makeText(this, "QQ号格式不正确（5-12位）", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "密码长度至少8位", Toast.LENGTH_SHORT).show()
            return
        }
        if (code.length != 6) {
            Toast.makeText(this, "请输入6位验证码", Toast.LENGTH_SHORT).show()
            return
        }
        if (!binding.registerAgreeBox.isChecked) {
            Toast.makeText(this, getString(R.string.please_agree), Toast.LENGTH_SHORT).show()
            return
        }

        binding.registerBtn.isEnabled = false
        binding.registerBtn.text = getString(R.string.registering)

        RegisterRequest(applicationContext).register(qq, password, code) { success, data, msg ->
            runOnUiThread {
                binding.registerBtn.isEnabled = true
                binding.registerBtn.text = getString(R.string.register_button_text)

                if (success && data != null) {
                    // 注册成功，自动保存登录信息
                    MyApplication.sessionManager.saveUserSession(
                        token = data.token ?: "",
                        uid = data.uid ?: 0,
                        username = data.username ?: "",
                        xp = 0,
                        qq = data.qq ?: qq,
                        checkingDays = 0
                    )
                    Toast.makeText(this, "注册成功，欢迎加入！", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    val errorMsg = when {
                        msg?.contains("已存在") == true -> "该账号已被注册"
                        msg?.contains("过期") == true -> "验证码已过期，请重新获取"
                        msg?.contains("错误") == true -> "验证码错误"
                        else -> msg ?: "注册失败"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
