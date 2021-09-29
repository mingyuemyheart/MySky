package com.cxwl.weather.eye.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.common.CONST
import kotlinx.android.synthetic.main.layout_title2.*

/**
 * 用户手册
 */
class ActivityHandbook : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handbook)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }
	
}
