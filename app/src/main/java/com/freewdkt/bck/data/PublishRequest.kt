package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.R
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PublishRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun publish(
        zone: Int,
        title: String,
        message: String,
        imgUrl: String?,
        token: String,
        callback: (success: Boolean, msg: String?, xp: Int?, level: Int?) -> Unit
    ) {
        // 构建 JSON 请求体
        val json = buildJson(zone, title, message, imgUrl, token)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(PrivateApi.UPLOAD_URL)   // 在 PrivateApi 中定义
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, context.getString(R.string.network_error), null, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    if (json != null) {
                        val result = Gson().fromJson(json, PublishResponse::class.java)
                        if (result.status == "success") {
                            callback(true, null, result.xp, result.level)
                        } else {
                            callback(false, result.msg, null, null)
                        }
                    } else {
                        callback(false, context.getString(R.string.empty_content), null, null)
                    }
                } else {
                    callback(false, "HTTP ${response.code}", null, null)
                }
            }
        })
    }

    private fun buildJson(zone: Int, title: String, message: String, imgUrl: String?, token: String): String {
        val map = mutableMapOf<String, Any>()
        map["zone"] = zone
        map["title"] = title
        map["message"] = message
        map["token"] = token
        imgUrl?.takeIf { it.isNotBlank() }?.let { map["img"] = it }
        return Gson().toJson(map)
    }

    data class PublishResponse(
        val status: String,
        val msg: String? = null,
        val xp: Int? = null,
        val level: Int? = null
    )
}