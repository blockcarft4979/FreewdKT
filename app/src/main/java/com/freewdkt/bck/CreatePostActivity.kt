package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.freewdkt.bck.data.PublishRequest
import com.freewdkt.bck.databinding.ActivityCreatePostBinding

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.btnPublish.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val zoneId = intent.getIntExtra("zone", 1)  // 从跳转传入
            val token = MyApplication.sessionManager.getToken() ?: ""
            if (token.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return@setOnClickListener
            }

            // 显示进度条
            binding.progressBar.visibility = View.VISIBLE
            binding.btnPublish.isEnabled = false

            val request = PublishRequest(applicationContext)
            request.publish(zoneId, title, content, selectedImageUrl, token) { success, msg, xp, level ->
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPublish.isEnabled = true
                    if (success) {
                        Toast.makeText(this,getString(R.string.upload_succeed), Toast.LENGTH_SHORT).show()
                        //xp?.let { MyApplication.sessionManager.updateXp(it) }
                        finish()
                    } else {
                        Toast.makeText(this, msg ?: getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 添加图片按钮
        binding.btnAddImage.setOnClickListener {
            Toast.makeText(this, "正在优化！", Toast.LENGTH_SHORT).show()
//            var intent = Intent(this, FreewdBrowser::class.java).apply {
//                putExtra("link", ApiConstants.UPLOAD_IMG)
//            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}