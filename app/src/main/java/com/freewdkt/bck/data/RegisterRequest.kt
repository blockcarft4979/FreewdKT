package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RegisterRequest(private val context: Context) {
    private val client = OkHttpClient()

    fun register(
        qq: String,
        password: String,
        code: String,
        callback: (success: Boolean, registerResponse: RegisterResponseData?, msg: String?) -> Unit
    ) {
        val jsonMap = mapOf(
            "qq" to qq,
            "password" to password,
            "email" to "$qq@qq.com",
            "code" to code
        )
        val json = Gson().toJson(jsonMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(PrivateApi.REGISTER_URL)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val result = Gson().fromJson(responseBody, RegisterResponseData::class.java)
                        if (result.status == "success") {
                            callback(true, result, null)
                        } else {
                            callback(false, null, result.msg ?: "注册失败")
                        }
                    } catch (e: Exception) {
                        callback(false, null, "解析响应失败")
                    }
                } else {
                    val errorMsg = try {
                        val errBody = responseBody?.let {
                            Gson().fromJson(it, RegisterResponseData::class.java)
                        }
                        errBody?.msg ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                    callback(false, null, errorMsg)
                }
            }
        })
    }

    data class RegisterResponseData(
        val status: String,
        val msg: String? = null,
        val token: String? = null,
        val uid: Int? = null,
        val username: String? = null,
        val qq: String? = null
    )
}
