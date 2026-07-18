package com.a10miaomiao.bilimiao.comm.proxy

import kotlinx.serialization.Serializable

@Serializable
data class ProxyServerInfo (
    val name: String, // 服务器名字
    // EN: Server name
    val host: String, // 服务器host
    // EN: Server host
    val isTrust: Boolean, // 是否信任服务器(是否携带B站token)
    // EN: Whether to trust the server (whether to carry Bilibili token)
    val enableAdvanced: Boolean? = null, // 启用高级设置(自定义参数和请求头)
    // EN: Enable advanced settings (custom params and headers)
    val queryArgs: List<HttpQueryArg>? = null, // 自定义请求参数
    // EN: Custom request params
    val headers: List<HttpHeader>? = null, // 自定义请求头
    // EN: Custom request headers
) {
    @Serializable
    data class HttpQueryArg(
        val enable: Boolean,
        val key: String,
        val value: String,
    )
    @Serializable
    data class HttpHeader(
        val enable: Boolean,
        val name: String,
        val value: String,
    )
}