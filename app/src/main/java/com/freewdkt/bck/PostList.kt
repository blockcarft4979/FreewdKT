package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewdkt.bck.adapter.LoadingFooterAdapter
import com.freewdkt.bck.adapter.PostAdapter
import com.google.android.material.snackbar.Snackbar
import com.freewdkt.bck.data.PostRequest
import com.freewdkt.bck.databinding.ActivityPostListBinding
import androidx.recyclerview.widget.ConcatAdapter
import com.freewdkt.bck.data.PostDetail
import com.freewdkt.bck.requestconstants.PrivateApi

class PostList : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding
    private lateinit var adapter: PostAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private val loadingFooter = LoadingFooterAdapter()
    private var currentPage = 1
    private var isLoading = false
    private var hasMore = true
    private val limit = 12
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val zoneId = intent.getStringExtra("zoneId") ?: "1"

        // 初始化 RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = PostAdapter()
        concatAdapter = ConcatAdapter(adapter)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = concatAdapter

        //列表框事件
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.fab.hide()
                } else if (dy < 0) {
                    binding.fab.show()
                }
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = adapter.itemCount
                if (!isLoading && hasMore && lastVisibleItem >= totalItemCount - 1) {
                    currentPage++
                    fetchPosts(zoneId, currentPage)
                }
            }
        })
        // 加载帖子数据
        fetchPosts(zoneId,currentPage)
adapter.onItemClick = {post ->
    val intent = Intent(this, PostDetails::class.java).apply {
        putExtra("filename",post.link)
        putExtra("zone",zoneId)
        putExtra("url", PrivateApi.postDetailUrl(post.link,zoneId))   // 假设 Post 有 link 字段
    }
    startActivity(intent)
}
        // 悬浮按钮点击事件
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.post_new_post), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.yes)) {
                    openAppByPackageName("com.freewd.bck")
                }
                .setAnchorView(binding.fab)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    /**
     * 尝试打开指定包名的应用
     */
    fun openAppByPackageName(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
                true
            } else {
                Toast.makeText(this, "应用未安装", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun fetchPosts(zoneId: String, page: Int) {
        if (isLoading || !hasMore) return
        isLoading = true

            if (concatAdapter.adapters.size == 1) {
                concatAdapter.addAdapter(loadingFooter)
            }
        

        val request = PostRequest(applicationContext)
        request.fetchPosts(zoneId, page, limit) { posts, error ->
            runOnUiThread {
                isLoading = false

                if (concatAdapter.adapters.size > 1) {
                    concatAdapter.removeAdapter(loadingFooter)
                }

                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    if (page > 1) currentPage--
                } else if (posts != null) {
                    if (posts.isEmpty()) {
                        hasMore = false
                    } else {
                        if (page == 1) {
                            adapter.submitList(posts)
                        } else {
                            // 获取当前列表并追加
                            val currentList = adapter.currentList.toMutableList()
                            currentList.addAll(posts)
                            adapter.submitList(currentList)
                        }
                        if (posts.size < limit) hasMore = false
                    }
                }
            }
        }
    }
}