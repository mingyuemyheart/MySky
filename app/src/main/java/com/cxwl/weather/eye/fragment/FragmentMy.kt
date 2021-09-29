package com.cxwl.weather.eye.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.activity.ActivityAbout
import com.cxwl.weather.eye.activity.ShawnFacilityActivity
import com.cxwl.weather.eye.activity.ActivityMsgType
import com.cxwl.weather.eye.activity.ShawnSettingActivity
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.manager.DataCleanManager
import com.cxwl.weather.eye.utils.AutoUpdateUtil
import com.cxwl.weather.eye.utils.CommonUtil
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.fragment_my.*

/**
 * 我的
 */
class FragmentMy : BaseFragment(), OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        clPortrait.setOnClickListener(this)
        llMsyType.setOnClickListener(this)
        llClearCache.setOnClickListener(this)
        llAbout.setOnClickListener(this)
        llFacility.setOnClickListener(this)
        llVersion.setOnClickListener(this)

        tvUserName.text = MyApplication.USERNAME
        tvVersion.text = CommonUtil.getVersion(activity)
        getCache()
    }

    private fun getCache() {
        try {
            tvCache.text = "(${DataCleanManager.getCacheSize(activity)})"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 删除对话框
     * @param message 标题
     * @param content 内容
     */
    private fun dialogClearCache(message: String, content: String) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_delete, null)
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvMessage.text = message
        view.tvContent.text = content
        view.tvContent.visibility = View.VISIBLE
        view.tvNegtive.setOnClickListener { dialog.dismiss() }
        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            DataCleanManager.clearCache(activity)
            getCache()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
//            R.id.clPortrait -> startActivity(Intent(activity, ShawnSettingActivity::class.java))
            R.id.llMsyType -> {
                val intent = Intent(activity, ActivityMsgType::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, tvAbout.text.toString())
                startActivity(intent)
            }
            R.id.llClearCache -> dialogClearCache("清除缓存", "确定要清除缓存？")
            R.id.llAbout -> {
                val intent = Intent(activity, ActivityAbout::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, tvAbout.text.toString())
                startActivity(intent)
            }
            R.id.llVersion -> AutoUpdateUtil.checkUpdate(activity, activity, "108", getString(R.string.app_name), false)
            R.id.llFacility -> startActivity(Intent(activity, ShawnFacilityActivity::class.java))
        }
    }

}
