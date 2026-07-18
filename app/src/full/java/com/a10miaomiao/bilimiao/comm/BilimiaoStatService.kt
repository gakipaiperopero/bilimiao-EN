package com.a10miaomiao.bilimiao.comm

import android.app.Activity
import android.content.Context
import com.baidu.mobstat.StatService

/**
 * bilimiao统计服务
 * 目前接入百度移动统计
 */
// EN: Bilimiao statistics service
// EN: Currently uses Baidu Mobile Statistics
object BilimiaoStatService {

    fun recordException(context: Context, e: Throwable) {
        StatService.recordException(context, e)
    }

    fun setAuthorizedState(activity: Activity, agree: Boolean) {
        StatService.setAuthorizedState(activity, agree)
    }

    fun start(activity: Activity) {
        StatService.start(activity)
    }
    fun onResume(activity: Activity) {
        StatService.onResume(activity)
    }

    fun onPause(activity: Activity) {
        StatService.onPause(activity)
    }
}