package com.a10miaomiao.bilimiao.comm.utils

import kotlin.experimental.xor

/**
 * 简单加解密
 * 原理：a=a^b^b
 */
// EN: Simple encryption/decryption. Principle: a=a^b^b
// EN: Simple encryption/decryption
// EN: Principle: a=a^b^b
class MiaoEncryptDecrypt(
    val key: ByteArray,
) {

    /**
     * 加密
     */
    // EN: Encrypt
    // EN: Encrypt
    fun encrypt(original: ByteArray): ByteArray {
        val encryptByte = ByteArray(original.size)
        original.forEachIndexed { i, b ->
            encryptByte[i] = b xor key[i % key.size]
        }
        return encryptByte
    }

    /**
     * 解密
     */
    // EN: Decrypt
    // EN: Decrypt
    fun decrypt(original: ByteArray): ByteArray {
        return encrypt(original)
    }
}