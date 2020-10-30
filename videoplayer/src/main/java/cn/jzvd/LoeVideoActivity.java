package cn.jzvd;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoeVideoActivity extends AppCompatActivity
{
    private Jzvd.JZAutoFullscreenListener mSensorEventListener;
    private SensorManager mSensorManager;

    private JzvdStd jzVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loe_video_activity);

        mSensorEventListener = new Jzvd.JZAutoFullscreenListener();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        jzVideo = findViewById(R.id.jzVideo);

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        if(url == null) url = "";
        if(title == null) title = "";
        boolean loop = getIntent().getBooleanExtra("loop", false);
        boolean canSpeed = getIntent().getBooleanExtra("canSpeed", true);
        boolean canScale = getIntent().getBooleanExtra("canScale", true);
        float speed = getIntent().getFloatExtra("speed", 1f);

        jzVideo.setStaticFull(true);

        jzVideo.setCanSpeed(canSpeed);
        jzVideo.setCanScale(canScale);
        jzVideo.setSpeed(speed);

        JZDataSource jzDataSource = new JZDataSource(url, title);
        jzDataSource.looping = loop;

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
        Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Jzvd.goOnPlayOnResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
        JZUtils.clearSavedProgress(this, null);
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onDestroy()
    {
        jzVideo.release();
        super.onDestroy();
    }
}