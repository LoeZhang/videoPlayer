package cn.jzvd;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Bundle;

public class LoeVideoActivity extends Activity
{
    private JzvdStd jzVideo;

    private VideoPlayConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            config = (VideoPlayConfig) getIntent().getSerializableExtra("config");
        }catch (Exception e)
        {
        }

        if(config == null) config = new VideoPlayConfig(getIntent().getStringExtra("url"));

        if(config.isLandscape())
        {
            if(config.isSensor())
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }else
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }else
        {
            if(config.isSensor())
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }else
            {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        setContentView(R.layout.loe_video_activity);


        jzVideo = findViewById(R.id.jzVideo);


        jzVideo.setStaticFull(true);

        jzVideo.setCanSpeed(config.isCanSpeed());
        jzVideo.setCanScale(config.isCanScale());
        jzVideo.setSpeed(config.getSpeed());
        jzVideo.setShowUI(config.isShowUI());

        JZDataSource jzDataSource = new JZDataSource(config.getUrl(), config.getTitle());
        jzDataSource.looping = config.isLoop();

        jzVideo.setUp(jzDataSource, JzvdStd.SCREEN_FULLSCREEN);

        JZUtils.hideSystemUI(this);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                jzVideo.startVideo();
            }
        },10);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Jzvd.goOnPlayOnResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onDestroy()
    {
        JZUtils.clearSavedProgress(this, null);
        jzVideo.release();
        super.onDestroy();
    }
}