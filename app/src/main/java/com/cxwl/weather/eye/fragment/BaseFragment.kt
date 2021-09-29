package com.cxwl.weather.eye.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cxwl.weather.eye.view.MyDialog

open class BaseFragment : Fragment() {

    private var mDialog: MyDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    protected fun showDialog() {
        if (mDialog == null) {
            mDialog = MyDialog(activity)
        }
        mDialog!!.show()
    }

    protected fun cancelDialog() {
        if (mDialog != null) {
            mDialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelDialog() //解决activity已经销毁，而还在调用dialog
    }

}