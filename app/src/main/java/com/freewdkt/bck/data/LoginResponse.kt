package com.freewdkt.bck.data

import android.content.Context
import android.util.Log
import com.freewdkt.bck.R
import com.freewdkt.bck.requestconstants.PrivateApi
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginRequest(
    private val context: Context, private val qq: String, private val password: String
) {
    private val client = OkHttpClient()

    fun execute(callback: (Boolean, LoginResponse?, String?) -> Unit) {
        if (qq.isBlank() || password.isBlank()) {
            callback(false, null, context.getString(R.string.empty_password_account))
            return
        }

        val json = """
                {
                    "qq": "$qq",
                    "password": "$password"
                }
            """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toRequestBody(mediaType)

        val request = Request.Builder().url(PrivateApi.LOGIN_URL).post(body)
            .addHeader("Content-Type", "application/json").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, context.getString(R.string.empty_content))
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val result = Gson().fromJson(responseBody, LoginResponse::class.java)
                        Log.d("我是RequestData", " $result")
                        if (result.status == "success") {
                            callback(true, result, null)
                        } else {
                            callback(false, null, result.msg)
                        }
                    } else {
                        callback(false, null, context.getString(R.string.empty_content))
                    }
                } else {
                    callback(false, null, "HTTP ${response.code}")
                }
            }
        })
    }

}

// 登录响应数据类
data class LoginResponse(
    val status: String,
    val msg: String? = null,
    val token: String? = null,
    val uid: Int? = null,
    val username: String? = null,
    val xp: Int? = null,
    val lastCheckingDay: String? = null,
    val checkingDays: Int? = null,
    val expiresIn: Int? = null
)

fun verifyToken(context: Context, token : String?,callback: (isValid: Boolean, isNetworkError: Boolean,username: String?, message: String?) -> Unit) {
    val request = Request.Builder()
        .url(PrivateApi.VERIFY_TOKEN)
        .addHeader("Authorization", "Bearer $token")
        .get()
        .build()
     val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            //Log.d("woshiLog",e.message.toString())
            callback(false, true,null,context.getString(R.string.load_fail))
        }

        override fun onResponse(call: Call, response: Response) {
            //Log.d("woshiLog2",response.message)
            if (response.isSuccessful) {
                val json = response.body?.string()
                if (json != null) {
                    val obj = JSONObject(json)
                    val status = obj.getString("status")
                    if (status == "success") {
                        val username = obj.getString("username")
                        callback(true, false, username, null)
                    } else {
                        callback(false, false, null, obj.optString("msg"))
                    }
                } else {
                    callback(false,false,null, context.getString(R.string.empty_content))
                }
            } else {
                callback(false,false, null,"HTTP ${response.code}")
            }
        }
    })
}

