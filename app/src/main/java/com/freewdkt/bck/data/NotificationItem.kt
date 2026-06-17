package com.freewdkt.bck.data

// 通知数据类

data class NotificationItem(
    val link: String,
    val qq: String,
    val username: String,
    val zone: Int,
    val content: String,
    val replyType: String,
    val createdAt: String,
    val isRead: Boolean
)