package com.cxwl.weather.eye.utils

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.cxwl.weather.eye.activity.ActivityWelcome
import com.cxwl.weather.eye.common.CONST
import java.io.File

/**
 * 下载视频
 * @author shawn_sun
 */
object DownloadVideoUtil {

    fun intoDownloadManager(context: Context, dataUrl: String) {
        if (TextUtils.isEmpty(dataUrl)) {
            return
        }
        val dManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(dataUrl)
        val request = Request(uri)
        // 设置下载路径和文件名
        val fileName: String = dataUrl.substring(dataUrl.lastIndexOf("/") + 1) //获取文件名称
        val filePath = context.getExternalFilesDir(null).absolutePath+"/"+fileName
        Log.e("filePath", "filePath=$filePath,fileName=$fileName")
        request.setDestinationInExternalPublicDir(context.getExternalFilesDir(null).absolutePath, fileName)
        request.setDescription(fileName)
        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner()
        // 设置为可见和可管理
        request.setVisibleInDownloadsUi(true)
        val referneceId = dManager.enqueue(request)
        // 把当前下载的ID保存起来
        val sPreferences = context.getSharedPreferences("downLoadVideo", 0)
        sPreferences.edit().putLong("videoId", referneceId).apply()
        initBroadCast(context, referneceId, filePath)
    }

    /**
     * 注册广播监听系统的下载完成事件
     */
    private fun initBroadCast(context: Context, referneceId: Long, filePath: String) {
        val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//                if (id == referneceId) {
//                    val i = Intent(context, ActivityWelcome::class.java)
//                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    context.startActivity(i)
//                }
            }
        }
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(mReceiver, intentFilter)
    }
	
}
