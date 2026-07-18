package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class UserRelationApi {

    /**
     * 关注Up主
     */
    // EN: Follow uploader
    fun modify(
        mid: String,
        mode: Int, // 1为关注，2为取消关注
        // EN: 1: follow, 2: unfollow
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/modify")
        formBody = ApiHelper.createParams(
            "fid" to mid,
            "act" to mode.toString(),
//            "re_src" to "32",
        )
        method = MiaoHttp.POST
    }

    /**
     * 关注的up主
     */
    // EN: Followed uploaders
    fun followings(
        mid: String,
        pageNum: Int = 1,
        pageSize: Int = 50,
        order: String = "attention" // 最常访问排列：attention，关注顺序排列：留空
        // EN: attention: most visited, empty: follow order
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/relation/followings",
            "vmid" to mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }

    /**
     * 搜索关注的up主
     */
    // EN: Search followed uploaders
    fun search(
        mid: String,
        name: String,
        pageNum: Int = 1,
        pageSize: Int = 50,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/relation/followings/search",
            "vmid" to mid,
            "name" to name,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 关注分组
     */
    // EN: Follow groups
    fun tags() = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags")
    }

    /**
     * 分组详情
     */
    // EN: Group details
    fun tagDetail(
        tagId: Int, // 特别关注恒为-10,默认分组恒为0
        // EN: Special follow is always -10, default group is always 0
        order: String = "attention", // 最常访问排列：attention，关注顺序排列：留空
        // EN: attention: most visited, empty: follow order
        pageNum: Int = 1,
        pageSize: Int = 50,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag",
            "tagid" to tagId.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }

    /**
     * 创建分组
     */
    // EN: Create group
    fun tagCreate(
        tag: String, // 分组名
        // EN: Group name
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/create")
        formBody = ApiHelper.createParams(
            "tag" to tag,
        )
        method = MiaoHttp.POST
    }

    /**
     * 修改分组
     */
    // EN: Modify group
    fun tagUpdate(
        tagId: Int,
        tagName: String, // 分组名
        // EN: Group name
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/update")
        formBody = ApiHelper.createParams(
            "tagid" to tagId.toString(),
            "name" to tagName,
        )
        method = MiaoHttp.POST
    }

    /**
     * 删除分组
     */
    // EN: Delete group
    fun tagDelete(
        tagId: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/del")
        formBody = ApiHelper.createParams(
            "tagid" to tagId.toString(),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量复制关注到分组
     */
    // EN: Batch copy follows to group
    fun copyUsers(
        fids: List<String>, // 待复制的用户 mid 列表
        // EN: List of user mid to copy
        tagids: List<Int>, // 目标分组 id 列表
        // EN: List of target group ids
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/copyUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "tagids" to tagids.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量移动关注到分组
     */
    // EN: Batch move follows to group
    fun moveUsers(
        fids: List<String>, // 待移动的用户 mid 列表
        // EN: List of user mid to move
        beforeTagids: List<Int>, // 原分组 id 列表
        // EN: Original group id list
        afterTagids: List<Int>, // 新分组 id 列表
        // EN: New group id list
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/moveUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "beforeTagids" to beforeTagids.joinToString(","),
            "afterTagids" to afterTagids.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    fun addUsers(
        fids: List<String>, // 用户 mid 列表
        // EN: User mid list
        tagIds: List<Int>, // 分组 id 列表
        // EN: Group id list
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/addUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "tagids" to tagIds.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量查询用户与自己关系
     */
    // EN: Batch query user relations
    fun interrelations(
        fids: List<String>,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/interrelations",
            "fids" to fids.joinToString(",")
        )
    }

}