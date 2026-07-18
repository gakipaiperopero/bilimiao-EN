package cn.a10miaomiao.bilimiao.compose.common.auth

import kotlinx.serialization.json.JsonObject

/**
 * Geetest 验证码结果
 */
// EN: Geetest captcha result
data class GeetestResult(
    val geetest_challenge: String,
    val geetest_seccode: String,
    val geetest_validate: String,
)

/**
 * Geetest 验证回调
 */
// EN: Geetest verification callback
interface GeetestCallback {
    suspend fun onResult(result: GeetestResult): Boolean
    suspend fun getApiJson(): JsonObject?
}

/**
 * Geetest 验证器接口
 */
// EN: Geetest verifier interface
interface GeetestVerifier {
    fun startVerification(callback: GeetestCallback)
}
