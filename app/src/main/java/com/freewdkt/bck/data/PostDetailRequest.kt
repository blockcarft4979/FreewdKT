package com.freewdkt.bck.data

import android.content.Context
import android.util.Log
import com.freewdkt.bck.R
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class PostDetailRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun fetchPostDetail(url: String, callback: (PostDetail?, String?) -> Unit) {

        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    //Log.d("woshiData",json.toString())
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