package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageUserInfo(
    /**
     * 用户ID.
     */
    // EN: User ID
    val mid: Long,
    /**
     * 是否为粉丝，0-不是，1-是.
     */
    // EN: Whether a fan, 0: no, 1: yes
    val fans: Int,
    val nickname: String,
    val avatar: String,
    val follow: Boolean
)