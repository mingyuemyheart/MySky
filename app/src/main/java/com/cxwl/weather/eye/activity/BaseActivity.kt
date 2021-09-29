package com.cxwl.weather.eye.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import com.cxwl.weather.eye.view.MyDialog


open class BaseActivity : Activity() {

    private var mDialog: MyDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    protected fun showDialog() {
        if (mDialog == null) {
            mDialog = MyDialog(this)
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
