package com.freewdkt.bck.data

import android.content.Context
import com.freewdkt.bck.R
import com.freewdkt.bck.requestconstants.ApiConstants
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

class RequestData(private val context: Context) {
    private val client = OkHttpClient()
    private val cacheFile: File by lazy {
        val file = File(context.filesDir, FilePath.ZONE_CACHE)
        file.parentFile?.mkdirs()
        file
    }

    fun fetchZones(callback: (List<ZoneItem>?, String?) -> Unit) {
        val cached = loadCachedZones()
        if (cached != null) {
            callback(cached, null)
        }
        val request = Request.Builder().url(ApiConstants.ZONE_DATA).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cached == null) {
                    callback(null, context.getString(R.string.server_error))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    if (json != null) {
                        saveCache(json)
                        val zoneResponse = Gson().fromJson(json, ZoneResponse::class.java)
                        callback(zoneResponse.zone, null)
                    } else {
                        callback(null, context.getString(R.string.empty_content))
                    }
                } else {
                    callback(null, "HTTP ${response.code}")
                }
            }
        })
    }

    private fun loadCachedZones(): List<ZoneItem>? {
        return if (cacheFile.exists()) {
            try {
                val json = cacheFile.readText()
                val zoneResponse = Gson().fromJson(json, ZoneResponse::class.java)
                zoneResponse.zone
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null
    }

    private fun saveCache(json: String) {

        try {
            cacheFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}