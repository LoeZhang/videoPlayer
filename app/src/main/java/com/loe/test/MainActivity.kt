package com.loe.test

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.jzvd.JZDataSource
import cn.jzvd.JZUtils
import cn.jzvd.Jzvd
import cn.jzvd.Jzvd.JZAutoFullscreenListener
import cn.jzvd.JzvdStd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    private val mSensorEventListener: JZAutoFullscreenListener by lazy { JZAutoFullscreenListener() }
    private val mSensorManager: SensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val m3u8Url = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
        val m3u8Url1 = "http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4"
        val m3u8Url2 = "https://jzvd.nathen.cn/video/460bad24-170c5bc6956-0007-1823-c86-de200.mp4"
        val jzDataSource = JZDataSource(m3u8Url2, "测试视频")
//        jzDataSource.looping = true



        jzVideo.setUp(jzDataSource, JzvdStd.SCREEN_NORMAL)
//        jzVideo.setUp(jzDataSource, JzvdStd.SCREEN_NORMAL, JZMediaAliyun::class.java)




//        jzVideo.setEnableScale(false)
//        jzVideo.setEnableSpeed(false)
        jzVideo.setSpeed(1.1f)
    }

    override fun onResume()
    {
        super.onResume()
        val accelerometerSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(mSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Jzvd.goOnPlayOnResume()
    }

    override fun onPause()
    {
        super.onPause()
        mSensorManager.unregisterListener(mSensorEventListener)
        JZUtils.clearSavedProgress(this, null)
        Jzvd.goOnPlayOnPause()
    }

    override fun onBackPressed()
    {
        if (!Jzvd.backPress())
        {
            super.onBackPressed()
        }
    }

    override fun onDestroy()
    {
        jzVideo.release()
        super.onDestroy()
    }
}
