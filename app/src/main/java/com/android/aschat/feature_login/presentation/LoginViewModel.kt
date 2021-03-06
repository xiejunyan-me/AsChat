package com.android.aschat.feature_login.presentation

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.aschat.R
import com.android.aschat.common.Constants
import com.android.aschat.common.MyApplication
import com.android.aschat.common.network.translate.Translate
import com.android.aschat.common.services.socketio.CheckServicesAliveService
import com.android.aschat.feature_home.presentation.HomeActivity
import com.android.aschat.feature_host.presentation.HostActivity
import com.android.aschat.feature_login.domain.model.appconfig.ConfigItemStrStr
import com.android.aschat.feature_login.domain.model.coin.CoinGoodPromotion
import com.android.aschat.feature_login.domain.model.coin.GetCoinGood
import com.android.aschat.feature_login.domain.model.osspolicy.OssPolicy
import com.android.aschat.feature_login.domain.repo.LoginRepo
import com.android.aschat.feature_rongyun.MyConversationActivity
import com.android.aschat.feature_rongyun.rongyun.custom_message_kind.HyperLinkMessage
import com.android.aschat.feature_rongyun.rongyun.ui.MyHyperLinkMessageProvider
import com.android.aschat.feature_rongyun.rongyun.ui.MyTextMessageProvider
import com.android.aschat.feature_rongyun.rongyun.ui.MyUserInfoProvider
import com.android.aschat.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.rong.imkit.RongIM
import io.rong.imkit.config.ConversationListBehaviorListener
import io.rong.imkit.config.RongConfigCenter
import io.rong.imkit.conversation.messgelist.provider.TextMessageItemProvider
import io.rong.imkit.conversationlist.model.BaseUiConversation
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class LoginViewModel @Inject constructor(
    @Named("LoginRepo") private val repo: LoginRepo
): ViewModel() {

    fun onEvent(event: LoginEvents) {
        when (event) {
            is LoginEvents.LoginEvent -> {
                viewModelScope.launch {
                    // ??????
                    val oldUserId = SpUtil.get(event.context, SpConstants.USERID, "")
                    val androidId = AppUtil.getAndroidId(event.context)

                    // 1) ??????
                    val loginResponse = repo.login(Constants.OauthType, androidId)
                    if (loginResponse.code == 0) {
                        // ??????
                        if (loginResponse.data!!.isFirstRegister) {
                            // ??????????????????????????????????????????
                            SpUtil.putAndApply(event.context, SpConstants.USERINFO, JsonUtil.any2Json(loginResponse.data.userInfo))
                        }else {
                            // ??????????????????????????????????????????(?????????????????????)
                            SpUtil.putAndApply(event.context, SpConstants.USERINFO, JsonUtil.any2Json(loginResponse.data.userInfo))
                        }

                        if (oldUserId != loginResponse.data.userInfo.userId) {
                            // ??????user id
                            SpUtil.putAndApply(event.context, SpConstants.USERID, loginResponse.data.userInfo.userId)
                        }
                        val token = loginResponse.data.token
                        // ??????token
                        SpUtil.putAndApply(event.context, SpConstants.TOKEN, token)

                        supervisorScope {
                            // 2) ????????????
                            val jobStrategy = launch {
                                val strategyResponse = repo.getStrategy()
                                if (strategyResponse.code == 0) {
                                    // ??????
                                    // ????????????
                                    SpUtil.putAndApply(event.context, SpConstants.STRATEGY, JsonUtil.any2Json(strategyResponse.data!!))
                                }
                            }

                            // 3) ????????????????????????
                            val jobCoinGoodPromotion = launch {
                                val response = repo.getCoinGoodsPromotion(GetCoinGood(true, Constants.PayChannel))
                                if (response.code == 0) {
                                    // ??????
                                    // ??????????????????????????????
                                    val data = response.data
                                    if (data != null) {
                                        SpUtil.putAndApply(event.context, SpConstants.COIN_GOODS_PROMOTION, JsonUtil.any2Json(data))
                                    }else {
                                        // ??????data???null?????????????????????????????????
                                        SpUtil.putAndApply(event.context, SpConstants.COIN_GOODS_PROMOTION, JsonUtil.any2Json(CoinGoodPromotion()))
                                    }
                                    // ?????????????????????????????????
                                    SpUtil.putAndApply(event.context, SpConstants.COIN_GOODS_PROMOTION_TEMP_STAMP, System.currentTimeMillis())
                                }
                            }

                            jobCoinGoodPromotion.join()

                            // 4) ?????????????????? ????????????????????????????????????????????????
                            val jobCoinGoods = launch {
                                val coinGoodResponse = repo.getCoinGoods(GetCoinGood(true, Constants.PayChannel))
                                if (coinGoodResponse.code == 0) {
                                    // ??????
                                    // ????????????????????????
                                    SpUtil.putAndApply(event.context, SpConstants.COIN_GOODS, JsonUtil.any2Json(coinGoodResponse.data!!))
                                }
                            }

                            // 5) ??????app config
                            val jobAppConfig = launch {
                                val configResponse = repo.getAppConfig(SpUtil.get(event.context, SpConstants.CONFIG_VER, 0) as Int)
                                if (configResponse.code == 0) {
                                    // ??????
                                    // ????????????
                                    // NOTE ????????????????????????????????????
                                    if (configResponse.data != null) {
                                        // ?????????list??????????????????????????????????????????
                                        val list = configResponse.data.items
                                        // NOTE ??????????????????if???????????????if??????????????????
                                        if (list.isNotEmpty()) {
                                            // ?????????????????????
                                            SpUtil.putAndApply(
                                                event.context,
                                                SpConstants.CONFIG_VER,
                                                configResponse.data.ver.toInt()
                                            )
                                            // ????????????config
                                            SpUtil.putAndApply(
                                                event.context,
                                                SpConstants.CONFIG_List,
                                                JsonUtil.any2Json(list)
                                            )
                                            // ??????????????????key
                                            val microKey = list.first { configItemBase ->
                                                configItemBase.name == Constants.MicroTransName
                                            } as ConfigItemStrStr
                                            SpUtil.putAndApply(
                                                event.context,
                                                SpConstants.Microsoft_Translation_Key,
                                                microKey.data
                                            )
                                            // ????????????key
                                            val rckKey = list.first { configItemBase ->
                                                configItemBase.name == Constants.RckName
                                            } as ConfigItemStrStr
                                            SpUtil.putAndApply(
                                                event.context,
                                                SpConstants.Rck_Key,
                                                rckKey.data
                                            )
                                            // ????????????key
                                            val rtckKey = list.first { configItemBase ->
                                                configItemBase.name == Constants.RtckName
                                            } as ConfigItemStrStr
                                            SpUtil.putAndApply(
                                                event.context,
                                                SpConstants.Rtck_Key,
                                                rtckKey.data
                                            )
                                        }
                                    }
                                }
                                initRongyun(event.context, loginResponse.data.userInfo.rongcloudToken)
                            }

//                            6) ??????oss???????????????
                            val jobOss = launch {
                                val ossResponse = repo.getOssPolicy()
                                if (ossResponse.code == 0) {
                                    SpUtil.putAndApply(
                                        event.context,
                                        SpConstants.Oss_Policy,
                                        JsonUtil.any2Json(ossResponse.data!!)
                                    )
                                }else {
                                    SpUtil.putAndApply(
                                        event.context,
                                        SpConstants.Oss_Policy,
                                        JsonUtil.any2Json(OssPolicy())
                                    )
                                }
                            }
                            jobStrategy.join()
                            jobCoinGoods.join()
                            jobAppConfig.join()
                            jobOss.join()

                            // NOTE ?????????????????????
                            Translate.initTranslate()

                            // NOTE ?????????????????????????????????
                            event.context.startService(Intent(event.context, CheckServicesAliveService::class.java))

                            // end)??????
                            if (loginResponse.data.isFirstRegister) {
                                // ???????????????
                                event.navController.navigate(R.id.action_splashFragment_to_fastLoginFragment)
                            }else {
                                // ?????????????????????
                                //                            event.navController.navigate(R.id.action_splashFragment_to_homeActivity)
                                val intent = Intent(event.context, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                event.context.startActivity(intent)
                            }
                        }

                    }
                }
            }
            is LoginEvents.FastLogin2HomeActivity -> {
//                event.navController.navigate(R.id.action_fastLoginFragment_to_homeActivity)
                val intent = Intent(event.context, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                event.context.startActivity(intent)
            }
        }
    }

    private fun initRongyun(context: Context, token: String) {
        // NOTE ???????????????
        try {
            val rckKey = SpUtil.get(context, SpConstants.Rck_Key, "") as String
            RongIM.init(MyApplication.application, rckKey)
            LogUtil.d("?????????????????????")
        }catch (e: Exception) {
            LogUtil.d("?????????????????????   ${e.stackTrace}")
        }

        // NOTE ?????????????????????????????????????????????
        RongIM.setUserInfoProvider(
            MyUserInfoProvider
            {
                repo.getHostInfo(it)
            },
            true
        )

        // NOTE ???????????????????????????????????????
        RongIM.setConversationListBehaviorListener(object : ConversationListBehaviorListener {
            // ??????????????????
            override fun onConversationPortraitClick(
                context: Context,
                p1: Conversation.ConversationType?,
                userId: String?
            ): Boolean {
                LogUtil.d("?????????????????????????????? userId $userId")
                val intent = Intent(context, HostActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Constants.WhereFrom, Constants.FromRongyunList)
                    putExtra("userId", userId)
                }
                context.startActivity(intent)
                return true
            }

            override fun onConversationPortraitLongClick(
                p0: Context?,
                p1: Conversation.ConversationType?,
                p2: String?
            ): Boolean {
                return false
            }

            override fun onConversationLongClick(
                p0: Context?,
                p1: View?,
                p2: BaseUiConversation?
            ): Boolean {
                return false
            }

            override fun onConversationClick(
                p0: Context?,
                p1: View?,
                p2: BaseUiConversation?
            ): Boolean {
                return false
            }
        })

//        // NOTE ???????????????????????????
//        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationListActivity, HomeActivity::class.java)
        // NOTE ???????????????????????????
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, MyConversationActivity::class.java)
        // NOTE ???????????????????????????UI
        RongConfigCenter.conversationConfig().replaceMessageProvider(TextMessageItemProvider::class.java, MyTextMessageProvider())
        // NOTE ?????????????????????
        RongIMClient.registerMessageType(listOf(HyperLinkMessage::class.java))
        RongConfigCenter.conversationConfig().addMessageProvider(MyHyperLinkMessageProvider())

        // NOTE ????????????
        try {
            // SDK ????????????????????????????????????????????????????????????????????????connect() ??????????????????????????????????????????????????????????????????
            RongIM.connect(token, object : RongIMClient.ConnectCallback() {
                override fun onSuccess(p0: String?) {
                    LogUtil.d("????????????????????????   $p0")
                    // TODO: ?????????????????????????????????
                }

                override fun onError(p0: RongIMClient.ConnectionErrorCode?) {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    LogUtil.d("????????????????????????   ${p0.toString()}")
                }

                override fun onDatabaseOpened(p0: RongIMClient.DatabaseOpenStatus?) {
                    // ????????????????????????????????????????????? DATABASE_OPEN_SUCCESS ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    LogUtil.d("??????onDatabaseopened??????   ${p0.toString()}")
                }

            })
            LogUtil.d("??????????????????")
        }catch (e: Exception) {
            LogUtil.d("??????????????????   ${e.stackTrace}")
        }
    }
}
