package com.a10miaomiao.bilimiao.comm.network

import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore
import com.a10miaomiao.bilimiao.comm.platform.Base64Provider
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import bilibili.metadata.Metadata
import bilibili.metadata.device.Device
import bilibili.metadata.fawkes.FawkesReq
import bilibili.metadata.locale.Locale
import bilibili.metadata.locale.LocaleIds
import bilibili.metadata.network.Network
import bilibili.metadata.network.NetworkType
import bilibili.metadata.restriction.Restriction
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray

object BiliGRPCConfig {

    /**
     * 频道.
     */
    // EN: Channel
    val channel = "bilibili140";

    /**
     * 网络状况.
     */
    // EN: Network status
    val networkType = 2;

    /**
     * 未知.
     */
    // EN: Unknown
    val networkTF = 0;

    /**
     * 未知.
     */
    // EN: Unknown
    val networkOid = "46007";

    /**
     * 未知.
     */
//    val buvid = "XZFD48CFF1E68E637D0DF11A562468A8DC314";
    val buvid get() = BilimiaoCommCore.instance.getBilibiliBuvid()

    /**
     * 应用类型.
     */
    // EN: App type
    val mobileApp = "android_hd";

    /**
     * 移动平台.
     */
    // EN: Mobile platform
    val platform = "android";

    /**
     * 产品环境.
     */
    // EN: Product environment
    val envorienment = "prod";

    /**
     * 应用Id.
     * 1为手机安卓APP，5为安卓平板APP
     */
    // EN: App ID. 1 is mobile Android, 5 is Android tablet
    var appId = 5;

    /**
     * 国家或地区.
     */
    // EN: Country or region
    val region = "CN";

    /**
     * 语言.
     */
    // EN: Language
    val language = "zh";

    /**
     * 获取客户端在Fawkes系统中的信息标头.
     */
    // EN: Get client fawkes req header
    fun getFawkesreqBin(): String {
        val msg = FawkesReq(
            appkey = mobileApp,
            env = envorienment,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取元数据标头.
     */
    // EN: Get metadata header
    fun getMetadataBin(accessToken: String): String {
        val msg = Metadata(
            accessKey = accessToken,
            mobiApp = mobileApp,
            build = ApiHelper.BUILD_VERSION,
            channel = channel,
            buvid = buvid,
            platform = platform,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取设备标头.
     */
    // EN: Get device header
    fun getDeviceBin(): String {
        val msg = Device(
            appId = appId,
            mobiApp = mobileApp,
            build = ApiHelper.BUILD_VERSION,
            channel = channel,
            buvid = buvid,
            platform = platform,
            brand = PlatformProviders.deviceInfo.brand,
            model = PlatformProviders.deviceInfo.model,
            osver = PlatformProviders.deviceInfo.osVersion,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取网络标头.
     */
    // EN: Get network header
    fun getNetworkBin(): String {
        val msg = Network(
            type = NetworkType.WIFI,
            oid = networkOid,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取限制标头.
     */
    // EN: Get restriction header
    fun getRestrictionBin(): String {
        val msg = Restriction()
        return toBase64(msg.encodeToByteArray())
    }

    /**
     * 获取本地化标头.
     */
    // EN: Get locale header
    fun getLocaleBin(): String {
        val cLocale = LocaleIds(
            language = language,
            region = region,
        )
        val sLocale = LocaleIds(
            language = language,
            region = region,
        )
        val msg = Locale(
            cLocale = cLocale,
            sLocale = sLocale,
        )
        return toBase64(msg.encodeToByteArray())
    }

    /**
     *  Dalvik/2.1.0 (Linux; U; Android 12; 2201123C Build/V417IR) 1.45.0 os/android model/2201123C mobi_app/android_hd build/1450000 channel/bili innerVer/1450000 osVer/12 network/2
     */
    fun getSystemUserAgent(): String {
        return PlatformProviders.deviceInfo.systemUserAgent
    }

    /**
     * 将数据转换为Base64字符串.
     */
    // EN: Convert data to Base64 string
    fun toBase64(data: ByteArray): String {
        return PlatformProviders.base64.encodeToString(data, Base64Provider.NO_PADDING or Base64Provider.NO_WRAP)
    }
}
