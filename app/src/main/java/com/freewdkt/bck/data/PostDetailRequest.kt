package com.freewdkt.bck.data

import android.content.Context
import android.util.Log
import com.freewdkt.bck.R
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class PostDetailRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun fetchPostDetail(url: String, token: String?, callback: (PostDetail?, String?) -> Unit) {
        val builder = Request.Builder().url(url)
        if (!token.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        val request = builder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    if (json != null) {
                        val detail = Gson().fromJson(json, PostDetail::class.java)
                        callback(detail, null)
                    } else {
                        callback(null, context.getString(R.string.empty_content))
                    }
                } else {
                    callback(null, "HTTP ${response.code}")
                }
            }
        })
    }
}