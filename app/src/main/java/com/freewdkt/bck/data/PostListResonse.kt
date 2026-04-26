package com.freewdkt.bck.data

import android.content.Context
import android.util.Log
import com.freewdkt.bck.R
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.java

class PostRequest(private val context: Context) {
    private val client = OkHttpClient()
    fun fetchPosts(zoneId: String, page: Int = 1, limit: Int = 12, callback: (List<Post>?, String?) -> Unit) {
        val url = "${PrivateApi.postListUrl(zoneId)}&page=$page&limit=$limit"
       // Log.d("woshiUrl",url)
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //Log.d("woshiError",e.message.toString())
                callback(null, context.getString(R.string.server_error_with_code))
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    //Log.d("woshiData",json.toString())
                    if (json != null) {
                        val result = Gson().fromJson(json, PostListResponse::class.java)
                        callback(result.list, null)
                    } else {
                        callback(null, context.getString(R.string.empty_content))
                    }
                } else {
                    callback(null, "${response.code}")
                }
            }
        })
    }
}