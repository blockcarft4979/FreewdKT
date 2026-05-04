package com.freewdkt.bck

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.freewdkt.bck.adapter.ReplyAdapter
import com.freewdkt.bck.data.PostDetail
import com.freewdkt.bck.data.PostDetailRequest
import com.freewdkt.bck.data.Reply
import com.freewdkt.bck.data.SendCommentRequest
import com.freewdkt.bck.databinding.ActivityPostDetailsBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.requestconstants.PrivateApi
import com.freewdkt.bck.ui.dialog.CommentBottomSheetDialog
import com.freewdkt.bck.utils.formatRelativeTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import java.text.SimpleDateFormat
import java.util.*

class PostDetails : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var replyAdapter: ReplyAdapter
    private lateinit var markwon: Markwon

    // 成员变量，供评论时使用
    private lateinit var currentUrl: String
    private var zoneId: String = "1"
    private var filename: String = ""
    private var currentDetail: PostDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityPostDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        markwon = Markwon.builder(this)
            .usePlugin(TablePlugin.create(this))
            .usePlugin(GlideImagesPlugin.create(this))
            .build()

        replyAdapter = ReplyAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = replyAdapter

        // 从 Intent 获取必要参数并保存
        currentUrl = getPostUrl(intent) ?: return
        zoneId = intent.getStringExtra("zone") ?: "1"
        filename = intent.getStringExtra("filename") ?: ""

        // 加载详情
        loadPostDetail(currentUrl)

        // 悬浮按钮点击 → 发表评论
        binding.fab.setOnClickListener {
            showCommentDialog()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * 根据启动的 Intent 解析出最终的详情 URL（支持 Deep Link 和普通 Intent）
     */
    private fun getPostUrl(intent: Intent): String? {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                if (uri.scheme == "https" && uri.host == "f2.freewd.top") {
                    val filename = uri.getQueryParameter("filename")
                    val zone = uri.getQueryParameter("zone")
                    if (filename != null && zone != null) {
                        if (!MyApplication.sessionManager.isLoggedIn()) {
                            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            return null
                        }
                        return PrivateApi.postDetailUrl(filename, zone)
                    }
                } else if (uri.scheme == "freewd" && uri.host == "open_post" && uri.path == "/path") {
                    val filename = uri.getQueryParameter("filename")
                    val zone = uri.getQueryParameter("zone")
                    if (filename != null && zone != null) {
                        if (!MyApplication.sessionManager.isLoggedIn()) {
                            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            return null
                        }
                        return PrivateApi.postDetailUrl(filename, zone)
                    }
                }
            }
        }
        val url = intent.getStringExtra("url")?.takeIf { it.isNotBlank() } ?: ApiConstants.POST_ERROR
        return if (url.startsWith("http://") || url.startsWith("https://")) url else null
    }

    /**
     * 加载帖子详情（支持 fallback 备用数据）
     */
    private fun loadPostDetail(url: String) {
        binding.loadingProgress.visibility = View.VISIBLE
        PostDetailRequest(applicationContext).fetchPostDetail(url) { detail, error ->
            runOnUiThread {
                binding.loadingProgress.visibility = View.GONE
                if (error != null) {
                    val fallbackUrl = ApiConstants.POST_ERROR
                    if (url != fallbackUrl) {
                        loadPostDetail(fallbackUrl)
                    } else {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                } else if (detail != null) {
                    binding.postView.visibility = View.VISIBLE
                    currentDetail = detail          // 保存详情供评论插入使用
                    bindDetail(detail)
                }
            }
        }
    }

    /**
     * 将数据绑定到 UI
     */
    private fun bindDetail(detail: PostDetail) {
        binding.postView.visibility = View.VISIBLE

        // 头像
        val avatarUrl = detail.qq?.let { ApiConstants.userIcon(it) } ?: ""
        Glide.with(binding.avatar)
            .load(avatarUrl)
            .error(R.mipmap.icon)
            .into(binding.avatar)

        // 标题
        if (!detail.title.isNullOrBlank()) {
            binding.title.text = detail.title
            binding.title.visibility = View.VISIBLE
        } else {
            binding.title.visibility = View.GONE
        }

        // 正文（支持 Markdown）
        val message = detail.msg ?: ""
        if (detail.isMarkdown) {
            markwon.setMarkdown(binding.content, message)
        } else {
            binding.content.text = message
        }

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

        // 图片列表
        val imgList = detail.imageList
        if (imgList.isNotEmpty()) {
            binding.imageScroll.visibility = View.VISIBLE
            binding.imagesContainer.removeAllViews()
            imgList.forEach { imageUrl ->
                val imageView = ShapeableImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(240.dpToPx(), 200.dpToPx()).apply {
                        marginEnd = 8.dpToPx()
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this@PostDetails)
                        .load(imageUrl)
                        .transform(RoundedCorners(8.dpToPx()))
                        .placeholder(R.mipmap.icon)
                        .error(R.mipmap.icon)
                        .into(this)
                    setOnClickListener {
                        showImagePreview(imageUrl)
                    }
                }
                binding.imagesContainer.addView(imageView)
            }
        } else {
            binding.imageScroll.visibility = View.GONE
        }

        // 评论列表
        replyAdapter.submitList(detail.reply)
    }

    /**
     * 全屏查看图片（启动独立的 PictureView Activity）
     */
    private fun showImagePreview(imageUrl: String) {
        val intent = Intent(this, PictureView::class.java).apply {
            putExtra("imgUrl", imageUrl)
        }
        startActivity(intent)
    }

   
    private fun showCommentDialog() {
        val dialog = CommentBottomSheetDialog()
        dialog.setOnCommentSendListener { content ->
            sendComment(content)
        }
        dialog.show(supportFragmentManager, "CommentBottomSheet")
    }

    private fun sendComment(content: String) {
        val token = MyApplication.sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }

        // 显示加载进度（如果希望有提示）
        binding.loadingProgress.visibility = View.VISIBLE

        SendCommentRequest(applicationContext).sendComment(
            fileName = filename,
            zone = zoneId.toInt(),
            content = content,
            replyTo = null,
            token = token
        ) { success, msg ->
            runOnUiThread {
                binding.loadingProgress.visibility = View.GONE
                if (success) {
                    Toast.makeText(this, getString(R.string.upload_succeed), Toast.LENGTH_SHORT).show()

                    // 构造新评论对象
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val now = sdf.format(Date())
                    val currentQQ = MyApplication.sessionManager.getQq() ?: ""
                    val currentName = MyApplication.sessionManager.getUsername() ?: currentQQ

                    val newReply = Reply(
                        qq = currentQQ,
                        date = now,
                        content = content,
                        username = currentName,
                        trueQq = currentQQ,
                        reply = null
                    )

                    val oldList = currentDetail?.reply?.toMutableList() ?: mutableListOf()
                    oldList.add(0, newReply)
                    replyAdapter.submitList(oldList)
                    binding.recyclerView.smoothScrollToPosition(0)

                    currentDetail = currentDetail?.copy(reply = oldList)
                } else {
                    Toast.makeText(this, msg ?: getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}