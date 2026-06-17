package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LikePostRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun likePost(
        filename: String,
        zone: Int,
        token: String,
        callback: (success: Boolean, isLiked: Boolean?, likeCount: Int?, msg: String?) -> Unit
    ) {
        val jsonMap = mapOf(
            "filename" to filename,
            "zone" to zone
        )
        val json = Gson().toJson(jsonMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(PrivateApi.LIKE_POST_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val result = Gson().fromJson(responseBody, LikePostResponse::class.java)
                        if (result.status == "success") {
                            callback(true, result.likes, result.likecount, null)
                        } else {
                            callback(false, null, null, result.msg ?: "点赞失败")
                        }
                    } catch (e: Exception) {
                        callback(false, null, null, "解析响应失败")
                    }
                } else {
                    callback(false, null, null, "HTTP ${response.code}")
                }
            }
        })
    }

    data class LikePostResponse(
        val status: String,
        val likes: Boolean? = null,
        val likecount: Int? = null,
        val msg: String? = null
    )
}
