package com.android.aschat.feature_rongyun

import android.app.Dialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.aschat.R
import com.android.aschat.common.BaseActivity
import com.android.aschat.common.Constants
import com.android.aschat.feature_host.presentation.HostEvents
import com.android.aschat.feature_host.presentation.HostViewModel
import com.android.aschat.feature_rongyun.rongyun.model.ExtraInfo
import com.android.aschat.util.DialogUtil
import com.android.aschat.util.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import io.rong.imkit.conversation.ConversationFragment
import io.rong.imkit.conversation.ConversationViewModel
import io.rong.imkit.model.TypingInfo.TypingUserInfo
import io.rong.imkit.userinfo.RongUserInfoManager
import io.rong.imlib.model.Conversation
import java.util.*

/**
 * 自定义的ConversationActivity会话界面
 */
@AndroidEntryPoint
class MyConversationActivity : BaseActivity() {
    private var mTargetId: String? = null
    private var mConversationType: Conversation.ConversationType? = null
    private var mConversationFragment: ConversationFragment? = null
    private var conversationViewModel: ConversationViewModel? = null

    private val mViewModel: HostViewModel by viewModels()

    private lateinit var mBackImg: ImageView
    private lateinit var mTitleTv: TextView
    private lateinit var mTypingTv: TextView
    private lateinit var mVideoImg: ImageView
    private lateinit var mMoreImg: ImageView

    private lateinit var mFirstBlockDialog: Dialog
    private lateinit var mSecondBlockDialog: Dialog

    private val mLoadingDialog: Dialog by lazy {
        DialogUtil.createLoadingDialog(this)
    }

    override fun provideLayoutId() = R.layout.rongyun_conversation_acty

    override fun init() {
        // 设置状态栏纯色
        StatusBarUtil.setColorNoTranslucent(this, resources.getColor(R.color.pink3))
        if (this.intent != null) {
            mTargetId = this.intent.getStringExtra("targetId")
            val type = this.intent.getStringExtra("ConversationType")
            if (TextUtils.isEmpty(type)) {
                return
            }
            mConversationType = Conversation.ConversationType.valueOf(type!!.toUpperCase(Locale.US))
        }
        loadFragment()
        setCustomTitleBar()
        if (mTargetId == null) finish()
        initViewModel(mTargetId!!)
    }


    private fun initViewModel(userId: String) {
        // 初始化viewmodel
        mViewModel.onEvent(HostEvents.SendUserId(userId))

        conversationViewModel = ViewModelProvider(this).get(
            ConversationViewModel::class.java
        )
        conversationViewModel!!.typingStatusInfo.observe(
            this
        ) { typingInfo ->
            if (typingInfo != null) {
                if (typingInfo.conversationType == mConversationType && mTargetId == typingInfo.targetId) {
                    if (typingInfo.typingList == null) {
                        mTitleTv.visibility = View.VISIBLE
                        mTypingTv.visibility = View.GONE
                    } else {
                        mTitleTv.visibility = View.GONE
                        mTypingTv.visibility = View.VISIBLE
                        val typing =
                            typingInfo.typingList[typingInfo.typingList.size - 1] as TypingUserInfo
                        if (typing.type == TypingUserInfo.Type.text) {
                            mTypingTv.text = getString(R.string.is_typing)
                        } else if (typing.type == TypingUserInfo.Type.voice) {
                            mTypingTv.text = getString(R.string.is_speaking)
                        }
                    }
                }
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyEvent.KEYCODE_BACK == event.keyCode && mConversationFragment != null && !mConversationFragment!!.onBackPressed()) {
            finish()
        }
        return false
    }

    /**
     * 自定义titlebar的相关设置
     */
    private fun setCustomTitleBar() {
        mBackImg = findViewById(R.id.conversation_back)
        mTitleTv = findViewById(R.id.conversation_title)
        mTypingTv = findViewById(R.id.conversation_title_typing)
        mVideoImg = findViewById(R.id.conversation_title_video)
        mMoreImg = findViewById(R.id.conversation_title_more)

        mBackImg.apply {
            setOnClickListener {
                if (mConversationFragment != null && !mConversationFragment!!.onBackPressed()) {
                    finish()
                }
            }
        }

        mTitleTv.apply {
            if (!TextUtils.isEmpty(mTargetId) && mConversationType == Conversation.ConversationType.GROUP) {
                val group = RongUserInfoManager.getInstance().getGroupInfo(mTargetId)
                text = (if (group == null) mTargetId else group.name)
            } else {
                val userInfo = RongUserInfoManager.getInstance().getUserInfo(mTargetId)
                text = (if (userInfo == null) mTargetId else userInfo.name)
            }
        }

        mVideoImg.setOnClickListener {

        }

        mMoreImg.setOnClickListener {
            // 打开举报屏蔽弹窗
            mSecondBlockDialog = DialogUtil.createSecondBlockDialog(
                this,
                mViewModel.blocked.value?:false,
                {
                    mViewModel.onEvent(HostEvents.ClickReport(Constants.Politiclsensitive))
                    it.dismiss()
                },
                {
                    mViewModel.onEvent(HostEvents.ClickReport(Constants.Falsegender))
                    it.dismiss()
                },
                {
                    mViewModel.onEvent(HostEvents.ClickReport(Constants.Fraud))
                    it.dismiss()
                },
                {
                    mViewModel.onEvent(HostEvents.ClickBlock)
                    it.dismiss()
                },
                {
                    mViewModel.onEvent(HostEvents.ClickReport())
                    it.dismiss()
                },
                {
                    it.dismiss()
                },
            )
            mFirstBlockDialog = DialogUtil.createFirstBlockDialog(
                this,
                mViewModel.follow.value?:false,// 一般来说不为null
                mViewModel.blocked.value?:false,
                {
                    // 点击关注
                    mViewModel.onEvent(HostEvents.ClickFollow)
                    it.dismiss()
                },
                {
                    // 屏蔽
                    mViewModel.onEvent(HostEvents.ClickBlock)
                    it.dismiss()
                },
                {
                    // report
                    it.dismiss()
                    mSecondBlockDialog.show()
                },
                {
                    it.dismiss()
                }).apply {
                show()
            }
        }

    }

    private fun loadFragment() {
        mConversationFragment = ConversationFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.rongyun_conversation_fragment, mConversationFragment!!, mConversationFragment!!::class.java.simpleName)
            .addToBackStack(mConversationFragment!!::class.java.simpleName)
            .commitAllowingStateLoss()
    }

    fun whenClickRechargeLink(extraInfo: ExtraInfo?) {
        mViewModel.onEvent(HostEvents.SubmitRecharge(
            activity = this,
            extraInfo = extraInfo,
            onStartSubmit = {
                mLoadingDialog.show()
            },
            onSuccess = {
                mLoadingDialog.dismiss()
            },
            onFail = {
                Toast.makeText(this, getString(R.string.abnormal_network), Toast.LENGTH_SHORT).show()
                mLoadingDialog.dismiss()
            }
        ))
    }
}