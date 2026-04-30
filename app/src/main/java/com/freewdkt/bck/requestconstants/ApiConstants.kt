package com.freewdkt.bck.requestconstants


object ApiConstants {
    const val BASE_URL = "https://f2.freewd.top/"
    const val BASE_REQUEST_URL = "${BASE_URL}app/api"
    const val USER_AGREEMENT = "${BASE_URL}documents/user_agreement.txt"
    const val UPLOAD_IMG = "https://photo.freewd.top/"
    const val ZONE_DATA = "$BASE_REQUEST_URL/zone.json"
    const val POST_ERROR = "$BASE_REQUEST_URL/error.json"
    fun userIcon(id: String): String {
        return "https://q.qlogo.cn/headimg_dl?dst_uin=${id}&spec=100"
    }
}

object PrivateApi {
    var BASE_URL = "https://app.freewd.top/app/api"

    var LOGIN_URL = "${BASE_URL}/login.php"
    var VERIFY_TOKEN = "${BASE_URL}/verify_token.php"
    var UPLOAD_URL = "${BASE_URL}/upload.php"
    fun postListUrl(zone: String): String {
        return "${BASE_URL}/get_posts.php?zone=${zone}"
    }

    fun postDetailUrl(filename: String, zoneId: String): String {
        return "${BASE_URL}/get_post.php?filename=${filename}&zone=${zoneId}"
    }
}