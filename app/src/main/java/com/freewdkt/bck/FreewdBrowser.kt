package com.freewdkt.bck

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.freewdkt.bck.adapter.ReplyAdapter
import com.freewdkt.bck.data.PostDetail
import com.freewdkt.bck.data.PostDetailRequest
import com.freewdkt.bck.databinding.ActivityFreewdBrowserBinding
import com.freewdkt.bck.databinding.ActivityPostDetailsBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.google.android.material.imageview.ShapeableImageView
import io.noties.markwon.Markwon

class FreewdBrowser : AppCompatActivity() {
    private lateinit var binding: ActivityFreewdBrowserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFreewdBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取链接
        val link = intent.getStringExtra("link") ?: ApiConstants.BASE_URL

        // 处理系统栏边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 配置 WebView
        binding.browser.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            webViewClient = WebViewClient()
            loadUrl(link)
        }
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

