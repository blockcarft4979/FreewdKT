package com.freewdkt.bck.data

data class UploadImgRequest(
    val status: String,
    val url: String? = null,
    val msg: String? = null
)