package com.freewdkt.bck

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.freewdkt.bck.databinding.ActivityUserAgreementBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.google.android.material.snackbar.Snackbar
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UserAgreementActivity : AppCompatActivity() {
    val client = okhttp3.OkHttpClient()
    private lateinit var binding: ActivityUserAgreementBinding

    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 先初始化 Markwon（表格+图片插件）
        markwon = Markwon.builder(this)
            .usePlugin(TablePlugin.create(this))
            .usePlugin(GlideImagesPlugin.create(this))
            .build()

        fetchUserAgreement()
    }

    private fun fetchUserAgreement() {
        binding.loadingProgress.visibility = View.VISIBLE
        val request = Request.Builder()
            .url(ApiConstants.USER_AGREEMENT)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    binding.loadingProgress.visibility = View.GONE
                    Snackbar.make(binding.root, getString(R.string.load_fail), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry)) {
                            fetchUserAgreement()
                            binding.loadingProgress.visibility = View.VISIBLE
                        }
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val markdownText = response.body?.string() ?: ""
                    runOnUiThread {
                        binding.loadingProgress.visibility = View.GONE
                        markwon.setMarkdown(binding.userAgreementText, markdownText)
                    }
                } else {
                    runOnUiThread {
                        binding.loadingProgress.visibility = View.GONE
                        binding.userAgreementText.text =
                            getString(R.string.server_error_with_code, response.code)
                    }
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}