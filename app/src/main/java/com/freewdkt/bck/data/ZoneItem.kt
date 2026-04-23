package com.freewdkt.bck.data

data class ZoneResponse(val zone: List<ZoneItem> = emptyList())
data class ZoneItem(
    val name: String = "",
    val description: String? = null,
    val icon: String? = null,
    val zone: Int? = null,
    val link: String? = null,
    val msg: String? = null
)
// PostItem.kt
data class PostListResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int,
    val list: List<Post>
)

data class Post(
    val title: String,
    val msg: String,
    val link: String,
    val date: String,
    val username: String,
    val qq: String
)