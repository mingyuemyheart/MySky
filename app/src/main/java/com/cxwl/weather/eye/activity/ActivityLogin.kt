package com.cxwl.weather.eye.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.utils.CommonUtil
import com.cxwl.weather.eye.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * 登录界面
 */
class ActivityLogin : BaseActivity(), OnClickListener {

    private val dataList: ArrayList<EyeDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        tvLogin.setOnClickListener(this)
        tvRegister.setOnClickListener(this)
        tvForgot.setOnClickListener(this)
        val params: ViewGroup.LayoutParams = ivLogo.layoutParams
        params.width = CommonUtil.widthPixels(this) * 2 / 3
        ivLogo.layoutParams = params
        etUserName.setText(MyApplication.USERNAME)
        etPwd.setText(MyApplication.PASSWORD)
        if (!TextUtils.isEmpty(etUserName.text.toString()) && !TextUtils.isEmpty(etPwd.text.toString())) {
            etUserName.setSelection(etUserName.text.toString().length)
            etPwd.setSelection(etPwd.text.toString().length)
            doLogin()
        }
    }

    private fun doLogin() {
        if (checkInfo()) {
            showDialog()
            okHttpLogin()
        }
    }

    /**
     * 验证用户信息
     */
    private fun checkInfo(): Boolean {
        if (TextUtils.isEmpty(etUserName.text.toString())) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etPwd.text.toString())) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * 登录
     */
    private fun okHttpLogin() {
        Thread {
            val param = JSONObject()
            param.put("userName", etUserName.text.toString())
            param.put("password", etPwd.text.toString())
            val json: String = param.toString()
            Log.e("login", json)
            val url = "${CONST.BASE_URL}/sky/system/user/login"
            Log.e("login", url)
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result: String = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        val dataObj = obj.getJSONObject("data")
                                        if (!dataObj.isNull("token")) {
                                            MyApplication.TOKEN = dataObj.getString("token")
                                        }
                                        if (!dataObj.isNull("userId")) {
                                            MyApplication.UID = dataObj.getString("userId")
                                        }
                                        if (!dataObj.isNull("operateDeviceStatus")) {
                                            MyApplication.USERTYPE = dataObj.getString("operateDeviceStatus")
                                        }
                                        MyApplication.USERNAME = etUserName.text.toString()
                                        MyApplication.PASSWORD = etPwd.text.toString()
                                        MyApplication.saveUserInfo(this@ActivityLogin)
                                        val intent = Intent(this@ActivityLogin, ActivityMain::class.java)
                                        val bundle = Bundle()
                                        bundle.putParcelableArrayList("dataList", dataList as ArrayList<out Parcelable?>)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityLogin, errorMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvLogin -> doLogin()
            R.id.tvRegister -> startActivityForResult(Intent(this, ShawnRegisterActivity::class.java), 1001)
            R.id.tvForgot -> startActivityForResult(Intent(this, ShawnForgetPwdActivity::class.java), 1002)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    etUserName.setText(MyApplication.USERNAME)
                    etPwd.setText(MyApplication.PASSWORD)
                    if (!TextUtils.isEmpty(etUserName.text.toString()) && !TextUtils.isEmpty(etPwd.text.toString())) {
                        etUserName.setSelection(etUserName.text.toString().length)
                        etPwd.setSelection(etPwd.text.toString().length)
                        doLogin()
                    }
                }
                1002 -> {
                    etUserName.setText(MyApplication.USERNAME)
                    etPwd.setText("")
                    etPwd.requestFocus()
                }
            }
        }
    }

}
