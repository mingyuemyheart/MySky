package com.cxwl.weather.eye.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.layout_title2.*

/**
 * 帮助关于
 */
class ActivityAbout : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        llUse.setOnClickListener(this)
        llFeedBack.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

        tvVersion.text = getString(R.string.app_name)+"V${CommonUtil.getVersion(this)}"
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.llUse -> {
                val intent = Intent(this, ActivityHandbook::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, tvUse.text.toString())
                startActivity(intent)
            }
            R.id.llFeedBack -> {
                val intent = Intent(this, ActivityFeedback::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, tvFeedback.text.toString())
                startActivity(intent)
            }
        }
    }
	
}
