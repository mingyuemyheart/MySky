package com.cxwl.weather.eye.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cxwl.weather.eye.R
import kotlinx.android.synthetic.main.layout_title2.*

/**
 * 消息
 */
class FragmentMsg : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_msg, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        tvTitle.text = "消息"
        llBack.visibility = View.GONE
    }

}