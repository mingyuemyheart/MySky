package com.cxwl.weather.eye.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.adapter.AdapterFeedback
import com.cxwl.weather.eye.common.CONST
import com.cxwl.weather.eye.common.MyApplication
import com.cxwl.weather.eye.dto.EyeDto
import com.cxwl.weather.eye.dto.RemoveListener
import com.cxwl.weather.eye.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.layout_title2.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * 意见反馈
 */
class ActivityFeedback : BaseActivity(), OnClickListener, RemoveListener {

    private var mAdapter: AdapterFeedback? = null
    private val dataList: ArrayList<EyeDto> = ArrayList()
    private val maxCount1 = 4
    private var feedType = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        initWidget()
        initGridView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        etContent.addTextChangedListener(contentWatcher)
        tvSubmit.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }
        rb1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "1"
                rb1.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                rb2.isChecked = false
                rb2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb3.isChecked = false
                rb3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb4.isChecked = false
                rb4.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb5.isChecked = false
                rb5.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb6.isChecked = false
                rb6.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
        }
        rb2.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "2"
                rb1.isChecked = false
                rb1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb2.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                rb3.isChecked = false
                rb3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb4.isChecked = false
                rb4.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb5.isChecked = false
                rb5.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb6.isChecked = false
                rb6.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
        }
        rb3.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "3"
                rb1.isChecked = false
                rb1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb2.isChecked = false
                rb2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb3.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                rb4.isChecked = false
                rb4.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb5.isChecked = false
                rb5.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb6.isChecked = false
                rb6.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
        }
        rb4.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "4"
                rb1.isChecked = false
                rb1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb2.isChecked = false
                rb2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb3.isChecked = false
                rb3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb4.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                rb5.isChecked = false
                rb5.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb6.isChecked = false
                rb6.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
        }
        rb5.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "5"
                rb1.isChecked = false
                rb1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb2.isChecked = false
                rb2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb3.isChecked = false
                rb3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb4.isChecked = false
                rb4.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb5.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                rb6.isChecked = false
                rb6.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
        }
        rb6.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                feedType = "6"
                rb1.isChecked = false
                rb1.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb2.isChecked = false
                rb2.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb3.isChecked = false
                rb3.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb4.isChecked = false
                rb4.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb5.isChecked = false
                rb5.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                rb6.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            }
        }
    }

    /**
     * 输入内容监听器
     */
    private val contentWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun afterTextChanged(arg0: Editable) {
            if (etContent.text.isEmpty()) {
                tvTextCount!!.text = "0/200"
            } else {
                tvTextCount!!.text = "${etContent.text.length}/200"
            }
        }
    }

    private fun initGridView() {
        initAddPicButton(dataList, maxCount1)
        mAdapter = AdapterFeedback(this, dataList)
        gridView.adapter = mAdapter
        mAdapter!!.setRemoveListener(this)
        gridView.setOnItemClickListener { parent, view, position, id ->
            val data = dataList[position]
            if (TextUtils.isEmpty(data.pictureUrl)) {//点击了添加按钮
                val intent = Intent(this, ActivitySelectPicture::class.java)
                intent.putExtra("count", dataList.size)
                intent.putExtra("maxCount", maxCount1)
                startActivityForResult(intent, 1001)
            } else {//预览
                val intent = Intent(this, ActivityDisplayPicture::class.java)
                val bundle = Bundle()
                bundle.putParcelable("data", data)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        }
    }

    override fun removeComplete(position: Int) {
        dataList.removeAt(position)
        initAddPicButton(dataList, maxCount1)
        if (mAdapter != null) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 添加图片按钮
     */
    private fun initAddPicButton(list: ArrayList<EyeDto>, maxCount: Int) {
        for (i in 0 until list.size) {
            if (TextUtils.isEmpty(list[i].pictureUrl)) {
                list.removeAt(i)
            }
        }
        if (list.size < maxCount-1) {
            val dto = EyeDto()
            dto.drawable = R.drawable.icon_add_pic
            dto.pictureUrl = ""
            list.add(dto)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.tvSubmit -> okHttpPostFiles()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                1001 -> {
                    if (data != null) {
                        val bundle = data.extras
                        if (bundle != null) {
                            val list: ArrayList<EyeDto> = bundle.getParcelableArrayList("dataList")
                            for (i in list.size-1 downTo 0) {
                                dataList.add(0, list[i])
                            }
                            initAddPicButton(dataList, maxCount1)
                            if (mAdapter != null) {
                                mAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验信息
     */
    private fun checkPost() : Boolean {
        if (TextUtils.isEmpty(etContent.text.toString())) {
            Toast.makeText(this, "请补充详情", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etContent.text.toString().length < 6) {
            Toast.makeText(this, "请补充6个字以上详情", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(etMobile.text.toString())) {
            Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show()
            return false
        }

        var imgSize = 0
        for (i in 0 until dataList.size) {
            val dto = dataList[i]
            if (!TextUtils.isEmpty(dto.pictureUrl)) {
                imgSize++
            }
        }
        if (imgSize == 0) {
            Toast.makeText(this, "请上传详情照片", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * 上传图片
     */
    private fun okHttpPostFiles() {
        if (!checkPost()) {
            return
        }
        showDialog()
        Thread {
            val url = "${CONST.BASE_URL}/sky/system/fileOperat/uploadByFile"
            val builder = MultipartBody.Builder()
            for (i in 0 until dataList.size) {
                val data = dataList[i]
                if (!TextUtils.isEmpty(data.pictureUrl)) {
                    val file = File(data.pictureUrl)
                    if (file.exists()) {
                        builder.addFormDataPart("fileList", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                    }
                }
            }
            val body: RequestBody = builder.build()
            OkHttpUtil.enqueue(Request.Builder().url(url).post(body).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        cancelDialog()
                        Toast.makeText(this@ActivityFeedback, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        Log.e("submit-file", result)
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        if (!obj.isNull("data")) {
                                            var imgUrls = obj.getString("data")
                                            if (!TextUtils.isEmpty(imgUrls) && imgUrls.contains(",")) {
                                                imgUrls = imgUrls.replace(",", "|")
                                            }
                                            okHttpPostContent(imgUrls)
                                        }
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityFeedback, errorMessage, Toast.LENGTH_SHORT).show()
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

    /**
     * 报送内容
     */
    private fun okHttpPostContent(imgUrls: String) {
        if (!checkPost()) {
            return
        }
        showDialog()
        Thread {
            val url = "${CONST.BASE_URL}/sky/feedback/add"
            val param  = JSONObject()
            param.put("type", feedType)
            param.put("detail", etContent.text.toString())
            param.put("contact", etMobile.text.toString())
            param.put("imgUrl", imgUrls)
            val json : String = param.toString()
            Log.e("submit-content", json)
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, json)
            OkHttpUtil.enqueue(Request.Builder().post(body).url(url).addHeader("token", MyApplication.TOKEN).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@ActivityFeedback, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        Log.e("submit-content", result)
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("result")) {
                                    if (obj.getBoolean("result")) {
                                        Toast.makeText(this@ActivityFeedback, "提交成功！", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        if (!obj.isNull("errorMessage")) {
                                            val errorMessage = obj.getString("errorMessage")
                                            if (errorMessage != null) {
                                                Toast.makeText(this@ActivityFeedback, errorMessage, Toast.LENGTH_SHORT).show()
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

}
