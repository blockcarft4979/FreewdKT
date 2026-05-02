package com.freewdkt.bck

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.freewdkt.bck.databinding.ActivityPictureViewBinding
import com.github.chrisbanes.photoview.PhotoView

class PictureView : AppCompatActivity() {
    private lateinit var binding: ActivityPictureViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val imgUrl = intent.getStringExtra("imgUrl") ?: ""
        if (imgUrl.isEmpty()) {
            Toast.makeText(this, R.string.load_fail, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadPicture(imgUrl)
    }

    private fun loadPicture(imgUrl: String) {
        Glide.with(this)
            .load(imgUrl)
            .placeholder(R.mipmap.icon)
            .error(R.mipmap.icon)
            .into(binding.photoView)
    }
}