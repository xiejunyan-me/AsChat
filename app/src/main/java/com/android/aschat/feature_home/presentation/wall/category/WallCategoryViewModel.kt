package com.android.aschat.feature_home.presentation.wall.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.aschat.common.Constants
import com.android.aschat.feature_home.domain.model.wall.subtag.GetHostInfo
import com.android.aschat.feature_home.domain.model.wall.subtag.HostData
import com.android.aschat.feature_home.domain.repo.HomeRepo
import com.android.aschat.feature_home.domain.rv.ListState
import com.android.aschat.feature_login.domain.model.strategy.BroadcasterWallTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class WallCategoryViewModel @Inject constructor(@Named("HomeRepo") private val repo: HomeRepo): ViewModel() {

    /**
     * 保存当前category的所有tag的list
     */
    private lateinit var mTagHostsMap: MutableMap<String, MutableList<HostData>>

    /**
     * 保存当前tag已加载的条数
     */
    private lateinit var mTagLimitMap: MutableMap<String, Int>

    /**
     * 当前要展示的list
     */
    private val _nowTagHosts: MutableLiveData<MutableList<HostData>> = MutableLiveData(mutableListOf())
    val nowTagHosts: LiveData<MutableList<HostData>> = _nowTagHosts

    /**
     * 当前category的信息
     */
    private lateinit var mBroadcasterWallTag: BroadcasterWallTag

    /**
     * 当前tag
     */
    private var mTag: String = "All"

    /**
     * 标志当前的list是全新的，还是旧列表有新数据加在后面
     *
     * 0表示全新，1表示新数据在后面
     */
    var mListState: ListState = ListState.REPLACE
    private set

    fun onEvent(event: SubTagEvents) {
        when(event) {
            is SubTagEvents.InitSubTags -> {
                // 设置category的信息
                mBroadcasterWallTag = event.broadcasterWallTag
               // 初始化map，让列表为空先
               mTagHostsMap = mutableMapOf<String, MutableList<HostData>>().apply {
                   mBroadcasterWallTag.subTagList.forEach { subTag ->
                       this[subTag] = mutableListOf()
                   }
               }
               mTagLimitMap = mutableMapOf<String, Int>().apply {
                   mBroadcasterWallTag.subTagList.forEach { subTag ->
                       this[subTag] = Constants.HostWall_LimitPlus
                   }
               }
            }

            is SubTagEvents.ChangeTab -> {
                // 更改当前要展示的列表
                viewModelScope.launch {
                    mTag = event.tag
                    val tempList = mTagHostsMap[mTag]
                    if (tempList!!.isEmpty()) {
                        // 如果还没有加载过就加载这个subtag下面的数据，然后再赋值给_nowTagHosts
                        val newHostData: List<HostData> = repo.getHostData(
                            GetHostInfo(
                                category = mBroadcasterWallTag.tagName,
                                isRemoteImageUrl = true,
                                limit = Constants.HostWall_LimitPlus,
                                page = 1,
                                tag = mTag)
                        ).data
                        mTagHostsMap[mTag] = newHostData.toMutableList()
                        // 全部替换了
                        mListState = ListState.REPLACE
                        _nowTagHosts.postValue(mTagHostsMap[mTag])
                    }else {
                        // 全部替换了，用原来存好的数据就行
                        mListState = ListState.REPLACE
                        _nowTagHosts.postValue(mTagHostsMap[mTag])
                    }
                }
            }

            is SubTagEvents.WantRefresh -> {
                // 刷新数据
                viewModelScope.launch {
                    // 恢复limit
                    mTagLimitMap[mTag] = Constants.HostWall_LimitPlus
                    mListState = ListState.REPLACE
                    // 取新data
                    val newHostData: List<HostData> = repo.getHostData(
                        GetHostInfo(
                            category = mBroadcasterWallTag.tagName,
                            isRemoteImageUrl = true,
                            limit = Constants.HostWall_LimitPlus,
                            page = 1,
                            tag = mTag)
                    ).data
                    // 覆盖旧数据
                    mTagHostsMap[mTag] = newHostData.toMutableList()
                    _nowTagHosts.postValue(mTagHostsMap[mTag])
                }
            }

            is SubTagEvents.WantMore -> {
                // 增加数据
                viewModelScope.launch {
                    // 增加limit
                    mTagLimitMap[mTag] = mTagLimitMap[mTag]!! + Constants.HostWall_LimitPlus
                    mListState = ListState.ADD
                    // 请求数据
                    val newHostData: List<HostData> = repo.getHostData(
                        GetHostInfo(
                            category = mBroadcasterWallTag.tagName,
                            isRemoteImageUrl = true,
                            limit = mTagLimitMap[mTag]!!,
                            page = 1,
                            tag = mTag)
                    ).data
                    // 加在旧数据后面
                    mTagHostsMap[mTag]!!.apply {
                        addAll(newHostData)
                    }
                    // 去重， 留给rv去做
//                    mTagHostsMap[mTag] = mTagHostsMap[mTag]!!.distinctBy {
//                        it.userId
//                    }.toMutableList()
                    _nowTagHosts.postValue(mTagHostsMap[mTag]!!)
                }
            }
        }
    }
}