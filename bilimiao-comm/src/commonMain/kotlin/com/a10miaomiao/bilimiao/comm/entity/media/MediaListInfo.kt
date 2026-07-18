package com.a10miaomiao.bilimiao.comm.entity.media

import kotlinx.serialization.Serializable

@Serializable
data class MediaListInfo(
    val attr: Int,
    var cover: String,
    var intro: String,
    var title: String,
    var cover_type: Int,
    var ctime: Long,
    var fav_state: Int,
    var fid: Long,
    var id: String,
    var like_state: Int = 0,
    var media_count: Int,
    var mid: Long,
    var mtime: Long,
    var state: Int,
    var type: Int,
    var upper: MediaUpperInfo? = null,
) {
    // 0:公开,1:私有
    // EN: 0: public, 1: private
    val privacy: Int get() = attr and 1

    // 是否为默认收藏夹
    // EN: Whether it's the default favorites folder
    val isDefaultFav: Boolean get () = attr or 1 == 1

}
