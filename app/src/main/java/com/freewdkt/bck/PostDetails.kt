package com.freewdkt.bck

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.freewdkt.bck.adapter.ReplyAdapter
import com.freewdkt.bck.data.PostDetail
import com.freewdkt.bck.data.PostDetailRequest
import com.freewdkt.bck.databinding.ActivityPostDetailsBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.utils.formatRelativeTime
import com.google.android.material.imageview.ShapeableImageView
import io.noties.markwon.Markwon
import io.noties.markwon.image.coil.CoilImagesPlugin

class PostDetails : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var replyAdapter: ReplyAdapter
    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 初始化 Markwon 和适配器
        markwon = Markwon.builder(this)
            .usePlugin(CoilImagesPlugin.create(this))
            .build()

        replyAdapter = ReplyAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = replyAdapter

        // 获取参数
        val url = intent.getStringExtra("url") ?: ApiConstants.POST_EEROR
        val zoneId = intent.getStringExtra("zone") ?: "1"
        val filename = intent.getStringExtra("filename") ?: ""

        // 加载数据
        binding.loadingProgress.visibility = View.VISIBLE
        PostDetailRequest(applicationContext).fetchPostDetail(url) { detail, error ->
            runOnUiThread {
                binding.loadingProgress.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                } else if (detail != null) {
                    binding.postView.visibility = View.VISIBLE
                    bindDetail(detail)
                }
            }
        }

        // 悬浮按钮
        binding.fab.setOnClickListener {
            val postUrl = "freewd://open_post/path?filename=$filename&zone=$zoneId"
            Log.d("woshipostUrl", postUrl)
            Toast.makeText(this, "正在启动原版Free社区", Toast.LENGTH_SHORT).show()
            try {
                openDeepLink(postUrl)
            } catch (e: Exception) {
                Toast.makeText(this, "打开失败${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun openDeepLink(deepLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        startActivity(intent)
    }

    private fun bindDetail(detail: PostDetail) {
        // 头像
        val avatarUrl = detail.qq?.let { ApiConstants.userIcon(it) } ?: ""
        binding.avatar.load(avatarUrl) {
            transformations(RoundedCornersTransformation(16f))
            placeholder(R.mipmap.icon)
            error(R.mipmap.icon)
        }

        // 标题
        if (!detail.title.isNullOrBlank()) {
            binding.title.text = detail.title
            binding.title.visibility = View.VISIBLE
        } else {
            binding.title.visibility = View.GONE
        }

        // 正文
        val message = detail.msg ?: ""
        if (detail.isMarkdown) {
            markwon.setMarkdown(binding.content, message)
        } else {
            binding.content.text = message
        }

        // 作者、日期、点赞数
        binding.username.text = detail.username
        binding.date.text = formatRelativeTime(detail.date)
        binding.likeCount.text = detail.likeCount

        // AI 总结
        if (!detail.aiSummary.isNullOrEmpty()) {
            binding.aiSummary.text = detail.aiSummary
            binding.aiSummaryContainer.visibility = View.VISIBLE
        } else {
            binding.aiSummaryContainer.visibility = View.GONE
        }

        // 图片
        val imgList = detail.imageList
        if (imgList.isNotEmpty()) {
            binding.imageScroll.visibility = View.VISIBLE
            binding.imagesContainer.removeAllViews()
            imgList.forEach { imageUrl ->
                val imageView = ShapeableImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(200.dpToPx(), 200.dpToPx()).apply {
                        marginEnd = 8.dpToPx()
                    }
                    load(imageUrl) {
                        transformations(RoundedCornersTransformation(8f))
                    }
                }
                binding.imagesContainer.addView(imageView)
            }
        } else {
            binding.imageScroll.visibility = View.GONE
        }

        // 评论
        replyAdapter.submitList(detail.reply)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}