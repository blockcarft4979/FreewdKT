package com.freewdkt.bck.data

import com.google.gson.annotations.SerializedName

data class PostDetail(
    @SerializedName("img")
    val img: Any? = null,
    val title: String? = null,
    val msg: String? = null,
    val qq: String? = null,
    val username: String,
    val date: String,
    @SerializedName("like_count")
    val likeCount: String,
    val reply: List<Reply>,
    @SerializedName("AI-summary")
    val aiSummary: String? = null,
    @SerializedName("is_markdown")
    val isMarkdown: Boolean,
) {
    val imageList: List<String>
        get() = when (img) {
            is List<*> -> img.filterIsInstance<String>()
            is String -> if (img.isNotBlank()) listOf(img) else emptyList()
            else -> emptyList()
        }
}


data class Reply(
    val qq: String,
    val date: String,
    val content: String,
    val username: String,
    @SerializedName("true_qq")
    val trueQq: String,
    val reply: String? = null
)