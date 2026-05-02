package com.freewdkt.bck

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.freewdkt.bck.databinding.ActivityFreewdBrowserBinding
import com.freewdkt.bck.requestconstants.ApiConstants

class FreewdBrowser : AppCompatActivity() {
    private lateinit var binding: ActivityFreewdBrowserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFreewdBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 可选：设置 Toolbar 并支持返回
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 获取链接
        val link = intent.getStringExtra("link") ?: ApiConstants.BASE_URL

        // 配置 WebView
        binding.browser.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            webViewClient = WebViewClient()

            // 添加进度监听
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress == 100) {
                        // 加载完成，隐藏进度条
                        binding.progressBar.visibility = android.view.View.GONE
                    } else {
                        // 加载中，显示进度条并更新进度
                        if (binding.progressBar.visibility != android.view.View.VISIBLE) {
                            binding.progressBar.visibility = android.view.View.VISIBLE
                        }
                        binding.progressBar.progress = newProgress
                    }
                }
            }

            loadUrl(link)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        binding.browser.apply {
            stopLoading()
            clearHistory()
            destroy()
        }
        super.onDestroy()
    }
}