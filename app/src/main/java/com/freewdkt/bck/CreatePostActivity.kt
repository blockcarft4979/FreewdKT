package com.freewdkt.bck

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.freewdkt.bck.data.PublishRequest
import com.freewdkt.bck.data.UploadImgRequest
import com.freewdkt.bck.databinding.ActivityCreatePostBinding
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUrl: String? = null
    private val uploadedImageUrls = mutableListOf<String>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 添加图片按钮
        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 发布按钮
        binding.btnPublish.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, R.string.empty_content_or_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val zoneId = intent.getIntExtra("zoneId", 1)
            val token = MyApplication.sessionManager.getToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnPublish.isEnabled = false
            binding.scrollView.visibility = View.INVISIBLE

            val request = PublishRequest(applicationContext)
            request.publish(
                zoneId,
                title,
                content,
                selectedImageUrl,
                token
            ) { success, msg, xp, level ->
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPublish.isEnabled = true
                    if (success) {
                        Toast.makeText(this, getString(R.string.upload_succeed), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        binding.scrollView.visibility = View.VISIBLE
                        Toast.makeText(this, msg ?: getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    private fun uploadImage(imageUri: Uri) {
        val token = MyApplication.sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show()
            return
        }

        // 显示上传进度（可以复用 progressBar 或另做一个）
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddImage.isEnabled = false

        // 将 Uri 转为本机临时文件
        val file = uriToFile(imageUri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody(file.extension?.let { "image/$it".toMediaType() } ?: "image/jpeg".toMediaType()))
            .build()

        val request = Request.Builder()
            .url(PrivateApi.UPLOAD_IMG)
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddImage.isEnabled = true
                    Toast.makeText(this@CreatePostActivity, "${getString(R.string.upload_failed)} ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddImage.isEnabled = true
                    if (response.isSuccessful) {
                        val json = response.body?.string()
                        val result = Gson().fromJson(json, UploadImgRequest::class.java)
                        if (result.status == "success" && !result.url.isNullOrEmpty()) {
                            selectedImageUrl = result.url
                            uploadedImageUrls.add(result.url)
                            addImageThumbnail(imageUri)
                            Toast.makeText(this@CreatePostActivity, getString(R.string.upload_succeed), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CreatePostActivity, result.msg ?:getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@CreatePostActivity, "HTTP ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun addImageThumbnail(imageUri: Uri) {
        val imageView = ShapeableImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(100.dpToPx(), 100.dpToPx()).apply {
                marginEnd = 8.dpToPx()
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
            // 可选：点击删除功能
            setOnLongClickListener {
                uploadedImageUrls.remove(selectedImageUrl)
                binding.llImages.removeView(this)
                if (uploadedImageUrls.isEmpty()) selectedImageUrl = null
                true
            }
        }
        binding.llImages.addView(imageView)
        binding.hsvImages.visibility = View.VISIBLE
    }


    private fun uriToFile(uri: Uri): File {
        val contentResolver: ContentResolver = contentResolver
        val tempFile = File(cacheDir, "img_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }


    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}

// 上传响应数据类
