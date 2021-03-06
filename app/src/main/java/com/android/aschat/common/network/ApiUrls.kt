package com.android.aschat.common.network

import java.util.*

object ApiUrls {
    /**
     * base url 【http://12345.1234:1234】
     */
    const val BASE_URL: String = "http://52.11.57.160:2030"

    /**
     * base 长连url
     */
    const val BASE_SOCKET_URL: String = "http://52.11.57.160:9001"

    /**
     * 登录
     */
    const val Login: String = "$BASE_URL/security/oauth"

    /**
     * 策略信息
     */
    const val GetStrategy = "$BASE_URL/config/getStrategy"

    /**
     * 搜索主播墙 主播列表，需要传一个json
     */
    const val GetHostList = "$BASE_URL/broadcaster/wall/search"

    /**
     * 添加关注
     */
    const val AddFriend = "$BASE_URL/user/addFriend"

    /**
     * 取消关注
     */
    const val CancelFriend = "$BASE_URL/user/unfriend"

    /**
     * 获得单个主播信息
     */
    const val GetUserInfo = "$BASE_URL/user/getUserInfo"

    /**
     * 获得主播额外信息：礼物，标签
     */
    const val GetExtraInfo = "$BASE_URL/user/getBroadcasterExtraInfo"

    /**
     * 获得关注的用户列表
     */
    const val GetFriends = "$BASE_URL/user/getFriendsListPage"

    /**
     * 获取礼物策略
     */
    const val GetGiftInfo = "$BASE_URL/gift/list"

    /**
     * oss信息获取
     */
    const val GetOssInfo = "$BASE_URL/user/oss/policy"

    /**
     * 获取金币商品链接
     */
    const val GetCoinGoods = "$BASE_URL/coin/goods/search"

    /**
     * 获取金币商品链接
     */
    const val GetCoinGoodsPromotion = "$BASE_URL/coin/goods/getPromotion"

    /**
     * 获取屏蔽列表
     */
    const val GetBlockedList = "$BASE_URL/report/complain/blockList"

    /**
     * 屏蔽或举报某人
     */
    const val BlockOrReport = "$BASE_URL/report/complain/insertRecord"

    /**
     * 接触屏蔽
     */
    const val CancelBlock = "$BASE_URL/report/complain/removeBlock"

    /**
     * 获取一个用户的在线状态
     */
    const val GetUserStatus = "$BASE_URL/user/getUserOnlineStatus"

    /**
     * 获取APP配置
     */
    const val GetAppConfig = "$BASE_URL/config/getAppConfig"

    /**
     * 把在oss服务器的头像的相对路径上传到livechat服务器
     */
    const val UpdateAvatar = "$BASE_URL/user/updateAvatar"

    /**
     * 保存用户基本信息
     */
    const val SaveUserBasicInfo = "$BASE_URL/user/saveUserInfo"

    /**
     * 主播榜的主播排行数据
     */
    const val RankCharmData = "$BASE_URL/broadcaster/rank/search"

    /**
     * 用户榜的排行榜数据
     */
    const val RankRichData = "$BASE_URL/user/rank/search"

    /**
     * 查询一批用户的在线状态
     */
    const val GetUserStatusList = "$BASE_URL/user/getUserListOnlineStatus"

    /**
     * 判断充值链接有效性
     */
    const val CheckRechargeRight = "$BASE_URL/coin/recharge/checkBroadcasterInvitation"

    /**
     * 获取主播推的充值信息
     */
    const val GetRechargeInfo = "$BASE_URL/coin/goods/broadcasterInvitation"
}