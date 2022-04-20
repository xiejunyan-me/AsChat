package com.android.aschat.feature_home.presentation

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.*
import com.android.aschat.R
import com.android.aschat.common.Constants
import com.android.aschat.feature_home.domain.model.mine.EditDetail
import com.android.aschat.feature_home.domain.model.mine.HomeUserListItem
import com.android.aschat.feature_home.domain.model.wall.subtag.HostData
import com.android.aschat.feature_home.domain.repo.HomeRepo
import com.android.aschat.feature_host.domain.model.hostdetail.userinfo.HostInfo
import com.android.aschat.feature_host.presentation.HostActivity
import com.android.aschat.feature_login.domain.model.login.UserInfo
import com.android.aschat.feature_login.domain.model.strategy.BroadcasterWallTag
import com.android.aschat.feature_login.domain.model.strategy.StrategyData
import com.android.aschat.util.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeViewModel @Inject constructor(@Named("HomeRepo") private val repo: HomeRepo, @Named("Context") val context: Context) : ViewModel() {

    // 个人资料界面

    private val _userInfo: MutableLiveData<UserInfo> = MutableLiveData(JsonUtil.json2Any(SpUtil.get(context, SpConstants.USERINFO, "") as String, UserInfo::class.java))
    val userInfo: LiveData<UserInfo> = _userInfo

    val avatarUrl: LiveData<String> = Transformations.map(_userInfo){
        it.avatarUrl
    }
    val coin: LiveData<String> = Transformations.map(_userInfo) {
        it.availableCoins.toString()
    }
    val nickName: LiveData<String> = Transformations.map(_userInfo) {
        it.nickname
    }

    private val _userItemList: MutableLiveData<List<HomeUserListItem>> = MutableLiveData(
        mutableListOf(
            HomeUserListItem(imageId = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_coin, null), text = context.getString(R.string.coin), cornText = _userInfo.value!!.availableCoins.toString()) {
                  LogUtil.d("click coin")
            },
            HomeUserListItem(imageId = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_customer, null), text = context.getString(R.string.custom_services)) {
                //
            },
            HomeUserListItem(imageId = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_blocked, null), text = context.getString(R.string.block_list)) {
                //
            },
            HomeUserListItem(imageId = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_about, null), text = context.getString(R.string.about)) {
                //
            },
            HomeUserListItem(imageId = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_setting, null), text = context.getString(R.string.setting)) {
                //
            }
        )
    )
    val userItemList: LiveData<List<HomeUserListItem>> = _userItemList

    // 个人资料编辑页

    private val _userEditDetail: MutableLiveData<EditDetail> = MutableLiveData(
        EditDetail()
    )
    val userEditDetail: LiveData<EditDetail> = _userEditDetail

    // 主播墙界面

    // 策略数据
    val wallStrategyData: StrategyData = JsonUtil.json2Any(SpUtil.get(context, SpConstants.STRATEGY, "") as String, StrategyData::class.java)

    // tags
    val parentTagList: List<BroadcasterWallTag> = wallStrategyData.broadcasterWallTagList

    init {
        // 给coin增加监听，改变时修改rv
        coin.observeForever {
            _userItemList.postValue(_userItemList.value.apply {
                this!![0].cornText = it
            })
        }
    }

    fun onEvent(event: HomeEvents) {
        when (event) {
            is HomeEvents.ToEditFragment -> {
                event.navController.navigate(R.id.action_userFragment_to_userEditFragment)
            }
            is HomeEvents.ShowTimePicker -> {
                PickerUtil.showTimePicker(event.fm) {datePickerDialog: DatePickerDialog, y: Int, m: Int, d: Int ->
                    _userEditDetail.postValue(_userEditDetail.value!!.copy(birthday = "$y-$m-$d"))
                }
            }
            is HomeEvents.ShowCountryPicker -> {
                PickerUtil.showCountryPicker(context = context, fm = event.fm) { name,  code,  dialCode,  flagDrawableResID ->
                    _userEditDetail.postValue(_userEditDetail.value!!.copy(country = name))
                }
            }
            is HomeEvents.ChangeHead -> {
                _userEditDetail.postValue(_userEditDetail.value!!.copy(head = event.head))
            }
            is HomeEvents.ExitUserEditFragment -> {
                event.navController.popBackStack()
            }
            is HomeEvents.ClickHost -> {
                val hostData = event.hostData
                val intent = Intent(context, HostActivity::class.java)
                // 把主播数据传过去
                intent.putExtra("hostData", JsonUtil.any2Json(hostData))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}