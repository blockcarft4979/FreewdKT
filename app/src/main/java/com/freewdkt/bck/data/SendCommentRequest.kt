package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.R
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SendCommentRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun sendComment(
        fileName: String,
        zone: Int,
        content: String,
        replyTo: String?,
        token: String,
        callback: (success: Boolean, msg: String?) -> Unit
    ) {
        if (content.isBlank()) {
            callback(false, context.getString(R.string.empty_content))
            return
        }

        val jsonMap = mutableMapOf<String, Any>()
        jsonMap["file_name"] = fileName
        jsonMap["zone"] = zone
        jsonMap["content"] = content
        jsonMap["token"] = token
        replyTo?.takeIf { it.isNotBlank() }?.let { jsonMap["reply"] = it }

        val json = Gson().toJson(jsonMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(PrivateApi.SEND_COMMENT_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val result = Gson().fromJson(responseBody, SendCommentResponse::class.java)
                    if (result.status == "success") {
                        callback(true, null)
                    } else {
                        callback(false, result.msg ?: context.getString(R.string.unknown_error))
                    }
                } else {
                    callback(false, "HTTP ${response.code}")
                }
            }
        })
    }

    data class SendCommentResponse(
        val status: String,
        val msg: String? = null
    )
}