package com.freewdkt.bck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewdkt.bck.adapter.LoadingFooterAdapter
import com.freewdkt.bck.adapter.PostAdapter
import com.freewdkt.bck.data.PostRequest
import com.freewdkt.bck.databinding.ActivityPostListBinding
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.android.material.snackbar.Snackbar

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

        enableEdgeToEdge()
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
        fetchPosts(zoneId, currentPage)
        adapter.onItemClick = { post ->
            val intent = Intent(this, PostDetails::class.java).apply {
                putExtra("filename", post.link)
                putExtra("zone", zoneId)
                putExtra("url", PrivateApi.postDetailUrl(post.link, zoneId))
            }
            startActivity(intent)
        }
        // 悬浮按钮点击事件
        binding.fab.setOnClickListener {
            var intent = Intent(this, CreatePostActivity::class.java).apply {
                putExtra("zoneId",zoneId)
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }



    private fun fetchPosts(zoneId: String, page: Int) {
        if (isLoading || !hasMore) return
        isLoading = true

        //首次加载（page == 1）时显示中央进度条
        if (page == 1) {
            binding.loadingProgress.visibility = View.VISIBLE
        }else {
            if (concatAdapter.adapters.size == 1) {
                concatAdapter.addAdapter(loadingFooter)
            }
        }
        val request = PostRequest(applicationContext)
        request.fetchPosts(zoneId, page, limit) { posts, error ->
            runOnUiThread {
                isLoading = false

                // 仅在首次加载时隐藏中央进度条
                if (page == 1) {
                    binding.loadingProgress.visibility = View.GONE
                }

                if (concatAdapter.adapters.size > 1) {
                    concatAdapter.removeAdapter(loadingFooter)
                }

                if (error != null) {
                    Snackbar.make(binding.root, getString(R.string.load_fail), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.retry)) { fetchPosts(zoneId, currentPage) }
                        .show()
                    if (page > 1) currentPage--
                } else if (posts != null) {
                    if (posts.isEmpty()) {
                        hasMore = false
                    } else {
                        if (page == 1) {
                            adapter.submitList(posts)
                        } else {
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