package com.cxwl.weather.eye.activity

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import com.cxwl.weather.eye.R
import com.cxwl.weather.eye.dto.EyeDto
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_display_picture.*
import java.io.File

/**
 * 图片预览
 */
class ActivityDisplayPicture : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_picture)
//        Sofia.with(this)
//                .invasionStatusBar() //设置顶部状态栏缩进
//                .statusBarBackground(Color.TRANSPARENT) //设置状态栏颜色
//                .invasionNavigationBar()
//                .navigationBarBackground(Color.TRANSPARENT)
        initWidget()
    }

    private fun initWidget() {
        if (intent.hasExtra("data")) {
            val data: EyeDto = intent.getParcelableExtra("data")
            if (data != null) {
                if (!TextUtils.isEmpty(data.pictureUrl)) {
                    if (data.pictureUrl.startsWith("http")) {
                        Picasso.get().load(data.pictureUrl).into(imageView)
                    } else {
                        val file = File(data.pictureUrl)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }

}
