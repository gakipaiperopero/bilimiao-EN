package com.a10miaomiao.bilimiao.comm.entity.message

import kotlinx.serialization.Serializable

@Serializable
data class MessageCursorInfo(
    /**
     * 是否已到末尾.
     */
    // EN: Whether reached the end
    val isEnd: Boolean = false,

    /**
     * 标识符.
     */
    // EN: Identifier
    val id: Long,

    /**
     * 发生时间.
     */
    // EN: Occurrence time
    val time: Long
)
