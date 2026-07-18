package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class BangumiAPI {

    /**
     * 番剧信息
     */
    // EN: Bangumi info
    fun seasonInfo(seasonId: String) = MiaoHttp.request {
        url = BiliApiService.biliBangumi("view/api/season",
            "season_id" to seasonId
        )
    }

    /**
     * 番剧信息V2
     */
    // EN: Bangumi info V2
    fun seasonInfoV2(seasonId: String, epId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/view/v2/app/season",
            "season_id" to seasonId.ifBlank { null },
            "ep_id" to epId.ifBlank { null },
        )
    }

    /**
     * 番剧剧集信息
     */
    // EN: Bangumi episode info
    fun seasonSection(seasonId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/web/season/section",
            "season_id" to seasonId
        )
    }

    /**
     * 剧集信息
     */
    // EN: Episode info
    fun episodeInfo(epId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/view/app/season",
            "ep_id" to epId)
    }

    /**
     * 追番列表
     */
    // EN: Follow list
    fun followList(
        type: String = "bangumi",
        status: Int,
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "pgc/app/follow/v2/$type",
            "status" to status.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString()
        )
    }

    /**
     * 收藏番剧
     */
    // EN: Follow bangumi
    fun followSeason(seasonId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/add")
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
        )
        method = MiaoHttp.POST
    }

    /**
     * 取消收藏番剧
     */
    // EN: Unfollow bangumi
    fun cancelFollow(seasonId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/del")
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
        )
        method = MiaoHttp.POST
    }

    /**
     * 设置状态
     */
    // EN: Set status
    fun setFollowStatus(seasonId: String, status: Int) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/status/update")
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
            "status" to status.toString(),
        )
        method = MiaoHttp.POST
    }

}