package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SendCodeRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun sendCode(
        qq: String,
        callback: (success: Boolean, msg: String?) -> Unit
    ) {
        val jsonMap = mapOf(
            "qq" to qq,
            "to" to qq
        )
        val json = Gson().toJson(jsonMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(PrivateApi.SEND_CODE_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val result = Gson().fromJson(responseBody, SendCodeResponse::class.java)
                        if (result.status == "success") {
                            callback(true, null)
                        } else {
                            callback(false, result.msg ?: "发送失败")
                        }
                    } catch (e: Exception) {
                        callback(false, "解析响应失败")
                    }
                } else {
                    val errorMsg = try {
                        val errBody = responseBody?.let {
                            Gson().fromJson(it, SendCodeResponse::class.java)
                        }
                        errBody?.msg ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                    callback(false, errorMsg)
                }
            }
        })
    }

    data class SendCodeResponse(
        val status: String,
        val msg: String? = null
    )
}
