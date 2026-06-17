package com.freewdkt.bck.utils

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // 保存登录信息
    fun saveUserSession(
        token: String,
        uid: Int,
        username: String,
        xp: Int,
        qq: String,
        checkingDays: Int
    ) {
        sharedPreferences.edit {
            putInt("checkingDays", checkingDays)
            putString("token", token)
            putInt("uid", uid)
            putString("username", username)
            putInt("xp", xp)
            putString("qq", qq)
        }
    }

    // 获取 token
    fun getToken(): String? = sharedPreferences.getString("token", null)

    // 获取 uid
    fun getUid(): Int = sharedPreferences.getInt("uid", 0)

    // 获取 username
    fun getUsername(): String? = sharedPreferences.getString("username", null)

    // 获取 xp
    fun getXp(): Int = sharedPreferences.getInt("xp", 0)

    fun getQq(): String? = sharedPreferences.getString("qq", null)

    // 判断是否已登录
    fun isLoggedIn(): Boolean = getToken() != null
    fun updateXp(xp: String) {
        sharedPreferences.edit { putString("xp", xp) }
    }

    fun getCheckInDays(): Int = sharedPreferences.getInt("checkingDays", 0)

    fun updateCheckInDays(days: Int) {
        sharedPreferences.edit { putInt("checkingDays", days) }
    }

    fun updateUsername(newUsername: String) {
        sharedPreferences.edit { putString("username", newUsername) }
    }

    // 清除所有用户信息（登出）
    fun clear() {
        sharedPreferences.edit { clear() }
    }

}