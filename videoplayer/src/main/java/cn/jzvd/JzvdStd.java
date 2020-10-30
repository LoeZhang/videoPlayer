package cn.jzvd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nathen
 * On 2016/04/18 16:15
 */
public class JzvdStd extends Jzvd {

    public static long LAST_GET_BATTERYLEVEL_TIME = 0;
    public static int LAST_GET_BATTERYLEVEL_PERCENT = 70;
    protected static Timer DISMISS_CONTROL_VIEW_TIMER;

    private static final int AUTO_TIME = 5000;

    private boolean canScale = true;

    private boolean canSpeed = true;

    public ImageView backButton;
    public ProgressBar loadingProgressBar;
//    public ProgressBar bottomProgressBar;
    public TextView titleTextView;
    public ImageView posterImageView;
    public ImageView tinyBackImageView;
    public LinearLayout batteryTimeLayout;
    public ImageView batteryLevel;
    public TextView videoCurrentTime;
    public TextView replayTextView;
    public TextView clarity;
    public TextView textScale;
    public TextView textSpeed;
    public PopupWindow clarityPopWindow;
    public PopupWindow scalePopWindow;
    public PopupWindow speedPopWindow;
    public TextView mRetryBtn;
    public LinearLayout mRetryLayout;
    public BroadcastReceiver battertReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int percent = level * 100 / scale;
                LAST_GET_BATTERYLEVEL_PERCENT = percent;
                setBatteryLevel();
                try {
                    jzvdContext.unregisterReceiver(battertReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;
    protected Dialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;
    protected Dialog mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;
    protected TextView mDialogVolumeTextView;
    protected ImageView mDialogVolumeImageView;
    protected Dialog mBrightnessDialog;
    protected ProgressBar mDialogBrightnessProgressBar;
    protected TextView mDialogBrightnessTextView;
    protected boolean mIsWifi;
    public BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean isWifi = JZUtils.isWifiConnected(context);
                if (mIsWifi == isWifi) return;
                mIsWifi = isWifi;
                if (!mIsWifi && !WIFI_TIP_DIALOG_SHOWED && state == STATE_PLAYING) {
                    startButton.performClick();
                    showWifiDialog();
                }
            }
        }
    };
    //doublClick 这两个全局变量只在ontouch中使用，就近放置便于阅读
    protected long lastClickTime = 0;
    protected long doubleTime = 200;
    protected ArrayDeque<Runnable> delayTask = new ArrayDeque<>();

    public JzvdStd(Context context) {
        super(context);
    }

    public JzvdStd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        batteryTimeLayout = findViewById(R.id.battery_time_layout);
//        bottomProgressBar = findViewById(R.id.bottom_progress);
        titleTextView = findViewById(R.id.title);
        backButton = findViewById(R.id.back);
        posterImageView = findViewById(R.id.poster);
        loadingProgressBar = findViewById(R.id.loading);
        tinyBackImageView = findViewById(R.id.back_tiny);
        batteryLevel = findViewById(R.id.battery_level);
        videoCurrentTime = findViewById(R.id.video_current_time);
        replayTextView = findViewById(R.id.replay_text);
        clarity = findViewById(R.id.clarity);
        textScale = findViewById(R.id.textScale);
        textSpeed = findViewById(R.id.textSpeed);
        mRetryBtn = findViewById(R.id.retry_btn);
        mRetryLayout = findViewById(R.id.retry_layout);

        if (batteryTimeLayout == null) {
            batteryTimeLayout = new LinearLayout(context);
        }
//        if (bottomProgressBar == null) {
//            bottomProgressBar = new ProgressBar(context);
//        }
        if (titleTextView == null) {
            titleTextView = new TextView(context);
        }
        if (backButton == null) {
            backButton = new ImageView(context);
        }
        if (posterImageView == null) {
            posterImageView = new ImageView(context);
        }
        if (loadingProgressBar == null) {
            loadingProgressBar = new ProgressBar(context);
        }
        if (tinyBackImageView == null) {
            tinyBackImageView = new ImageView(context);
        }
        if (batteryLevel == null) {
            batteryLevel = new ImageView(context);
        }
        if (videoCurrentTime == null) {
            videoCurrentTime = new TextView(context);
        }
        if (replayTextView == null) {
            replayTextView = new TextView(context);
        }
        if (clarity == null) {
            clarity = new TextView(context);
        }
        if (textScale == null) {
            textScale = new TextView(context);
        }
        if (textSpeed == null) {
            textSpeed = new TextView(context);
        }
        if (mRetryBtn == null) {
            mRetryBtn = new TextView(context);
        }
        if (mRetryLayout == null) {
            mRetryLayout = new LinearLayout(context);
        }

        posterImageView.setOnClickListener(this);
        backButton.setOnClickListener(this);
        tinyBackImageView.setOnClickListener(this);
        clarity.setOnClickListener(this);
        textScale.setOnClickListener(this);
        textSpeed.setOnClickListener(this);
        mRetryBtn.setOnClickListener(this);
        titleTextView.setOnClickListener(this);
    }

    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        if ((System.currentTimeMillis() - gobakFullscreenTime) < 200) {
            return;
        }

        if ((System.currentTimeMillis() - gotoFullscreenTime) < 200) {
            return;
        }


        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        titleTextView.setText(jzDataSource.title);
        setScreen(screen);
    }

    @Override
    public void changeUrl(JZDataSource jzDataSource, long seekToInAdvance) {
        super.changeUrl(jzDataSource, seekToInAdvance);
        titleTextView.setText(jzDataSource.title);
    }

    public void changeStartButtonSize(int size) {
        ViewGroup.LayoutParams lp = startButton.getLayoutParams();
        lp.height = size;
        lp.width = size;
        lp = loadingProgressBar.getLayoutParams();
        lp.height = size;
        lp.width = size;
    }

    @Override
    public int getLayoutId() {
        return R.layout.jz_layout_std;
    }

    @Override
    public void onStateNormal() {
        super.onStateNormal();
        changeUiToNormal();
    }

    @Override
    public void onStatePreparing() {
        super.onStatePreparing();
        changeUiToPreparing();
    }

    public void onStatePreparingPlaying() {
        super.onStatePreparingPlaying();
        changeUIToPreparingPlaying();
    }

    public void onStatePreparingChangeUrl() {
        super.onStatePreparingChangeUrl();
        changeUIToPreparingChangeUrl();
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        changeUiToPlayingClear();

        try {
            mediaInterface.setSpeed(speed);
        }catch (Exception e)
        {
        }
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
        changeUiToPauseShow();
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStateError() {
        super.onStateError();
        changeUiToError();
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        changeUiToComplete();
        cancelDismissControlViewTimer();
//        bottomProgressBar.setProgress(100);
    }

    @Override
    public void startVideo() {
        super.startVideo();
        registerWifiListener(getApplicationContext());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
//                    if (mChangePosition) {
//                        long duration = getDuration();
//                        int progress = (int) (mSeekTimePosition * 100 / (duration == 0 ? 1 : duration));
//                        bottomProgressBar.setProgress(progress);
//                    }

                    //加上延时是为了判断点击是否是双击之一，双击不执行这个逻辑
                    Runnable task = () -> {
                        if (!mChangePosition && !mChangeVolume) {
                            onClickUiToggle();
                        }
                    };
                    v.postDelayed(task, doubleTime + 20);
                    delayTask.add(task);
                    while (delayTask.size() > 2) {
                        delayTask.pollFirst();
                    }

                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastClickTime < doubleTime) {
                        for (Runnable taskItem : delayTask) {
                            v.removeCallbacks(taskItem);
                        }
                        if (state == STATE_PLAYING || state == STATE_PAUSE) {
                            Log.d(TAG, "doublClick [" + this.hashCode() + "] ");
                            startButton.performClick();
                        }
                    }
                    lastClickTime = currentTimeMillis;
                    break;
            }
        } else if (id == R.id.bottom_seek_progress) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelDismissControlViewTimer();
                    break;
                case MotionEvent.ACTION_UP:
                    startDismissControlViewTimer();
                    break;
            }
        }
        return super.onTouch(v, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.poster) {
            if(loadingProgressBar.getVisibility() == VISIBLE)
            {
                if (bottomContainer.getVisibility() == View.VISIBLE)
                {
                    changeUiToLoadingClear();
                } else {
                    changeUiToLoadingShow();
                }
            }else
            {
                clickPoster();
            }
        } else if (i == R.id.surface_container) {
            if(loadingProgressBar.getVisibility() == VISIBLE)
            {
                if (bottomContainer.getVisibility() == View.VISIBLE)
                {
                    changeUiToLoadingClear();
                } else {
                    changeUiToLoadingShow();
                }
            }else
            {
                clickSurfaceContainer();
                if (clarityPopWindow != null) {
                    clarityPopWindow.dismiss();
                }
                if (scalePopWindow != null) {
                    scalePopWindow.dismiss();
                }
            }
        } else if (i == R.id.back || i == R.id.title) {
            clickBack();
        } else if (i == R.id.back_tiny) {
            clickBackTiny();
        } else if (i == R.id.clarity) {
            clickClarity();
        } else if (i == R.id.textScale) {
            clickScale();
        } else if (i == R.id.textSpeed) {
            clickSpeed();
        } else if (i == R.id.retry_btn) {
            clickRetryBtn();
        }
    }

    protected void clickRetryBtn() {
        if (jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            Toast.makeText(jzvdContext, getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!jzDataSource.getCurrentUrl().toString().startsWith("file") && !
                jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_DIALOG_SHOWED) {
            showWifiDialog();
            return;
        }
        seekToInAdvance = mCurrentPosition;
        startVideo();
    }

    protected void clickClarity() {
        onCLickUiToggleToClear();

        LayoutInflater inflater = (LayoutInflater) jzvdContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.jz_layout_clarity, null);

        OnClickListener mQualityListener = v1 -> {
            int index = (int) v1.getTag();

//                this.seekToInAdvance = getCurrentPositionWhenPlaying();
            jzDataSource.currentUrlIndex = index;
//                onStatePreparingChangeUrl();

            changeUrl(jzDataSource, getCurrentPositionWhenPlaying());

            clarity.setText(jzDataSource.getCurrentKey().toString());
            for (int j = 0; j < layout.getChildCount(); j++) {//设置点击之后的颜色
                if (j == jzDataSource.currentUrlIndex) {
                    ((TextView) layout.getChildAt(j)).setTextColor(0XffF85959);
                } else {
                    ((TextView) layout.getChildAt(j)).setTextColor(0XffFFFFFF);
                }
            }
            if (clarityPopWindow != null) {
                clarityPopWindow.dismiss();
            }
        };

        for (int j = 0; j < jzDataSource.urlsMap.size(); j++) {
            String key = jzDataSource.getKeyFromDataSource(j);
            TextView clarityItem = (TextView) View.inflate(jzvdContext, R.layout.jz_layout_clarity_item, null);
            clarityItem.setText(key);
            clarityItem.setTag(j);
            layout.addView(clarityItem, j);
            clarityItem.setOnClickListener(mQualityListener);
            if (j == jzDataSource.currentUrlIndex) {
                clarityItem.setTextColor(0XffF85959);
            }
        }

        clarityPopWindow = new PopupWindow(layout, JZUtils.dip2px(jzvdContext, 200), LayoutParams.MATCH_PARENT, true);
        clarityPopWindow.setContentView(layout);
        clarityPopWindow.setAnimationStyle(R.style.pop_animation);
        clarityPopWindow.showAtLocation(textureViewContainer, Gravity.END, 0, 0);

        clarityPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                JZUtils.hideSystemUI(getContext());
            }
        });
//            int offsetX = clarity.getMeasuredWidth() / 3;
//            int offsetY = clarity.getMeasuredHeight() / 3;
//            clarityPopWindow.update(clarity, -offsetX, -offsetY, Math.round(layout.getMeasuredWidth() * 2), layout.getMeasuredHeight());
    }

    public static String getScaleName()
    {
        switch (Jzvd.VIDEO_IMAGE_DISPLAY_TYPE)
        {
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER:
                return "适应";
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT:
                return "拉伸";
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP:
                return "裁切";
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL:
                return "原始";
        }
        return "";
    }

    private void setScaleType(int type)
    {
        setVideoImageDisplayType(type);
        if (scalePopWindow != null)
        {
            scalePopWindow.dismiss();
        }
        textScale.setText(getScaleName());
    }

    /**
     * 点击缩放方式
     */
    protected void clickScale()
    {
        onCLickUiToggleToClear();

        LayoutInflater inflater = (LayoutInflater) jzvdContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.jz_layout_scale, null);

        layout.getChildAt(0).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setScaleType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
            }
        });
        layout.getChildAt(1).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setScaleType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT);
            }
        });
        layout.getChildAt(2).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setScaleType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP);
            }
        });
        layout.getChildAt(3).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setScaleType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL);
            }
        });

        ((TextView) layout.getChildAt(0)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(1)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(2)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(3)).setTextColor(0xffFFFFFF);
        switch (Jzvd.VIDEO_IMAGE_DISPLAY_TYPE)
        {
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER:
                ((TextView) layout.getChildAt(0)).setTextColor(0XffF85959);
                break;
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT:
                ((TextView) layout.getChildAt(1)).setTextColor(0XffF85959);
                break;
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP:
                ((TextView) layout.getChildAt(2)).setTextColor(0XffF85959);
                break;
            case Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL:
                ((TextView) layout.getChildAt(3)).setTextColor(0XffF85959);
                break;
        }

        scalePopWindow = new PopupWindow(layout, JZUtils.dip2px(jzvdContext, 180), LayoutParams.MATCH_PARENT, true);
        scalePopWindow.setContentView(layout);
        scalePopWindow.setAnimationStyle(R.style.pop_animation);
        scalePopWindow.showAtLocation(textureViewContainer, Gravity.END, 0, 0);

        scalePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                JZUtils.hideSystemUI(getContext());
            }
        });
    }

    private void setSpeedType(float speed)
    {
        setSpeed(speed);
        if (speedPopWindow != null)
        {
            speedPopWindow.dismiss();
        }
        textSpeed.setText(speed + "倍");
    }

    /**
     * 点击缩放方式
     */
    protected void clickSpeed()
    {
        onCLickUiToggleToClear();

        LayoutInflater inflater = (LayoutInflater) jzvdContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.jz_layout_speed, null);

        layout.getChildAt(0).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(1f);
            }
        });
        layout.getChildAt(1).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(1.1f);
            }
        });
        layout.getChildAt(2).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(1.2f);
            }
        });
        layout.getChildAt(3).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(1.5f);
            }
        });
        layout.getChildAt(4).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(2f);
            }
        });
        layout.getChildAt(5).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSpeedType(4f);
            }
        });

        ((TextView) layout.getChildAt(0)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(1)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(2)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(3)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(4)).setTextColor(0xffFFFFFF);
        ((TextView) layout.getChildAt(5)).setTextColor(0xffFFFFFF);
        switch ((int) (speed * 10))
        {
            case 10:
                ((TextView) layout.getChildAt(0)).setTextColor(0XffF85959);
                break;
            case 11:
                ((TextView) layout.getChildAt(1)).setTextColor(0XffF85959);
                break;
            case 12:
                ((TextView) layout.getChildAt(2)).setTextColor(0XffF85959);
                break;
            case 15:
                ((TextView) layout.getChildAt(3)).setTextColor(0XffF85959);
                break;
            case 20:
                ((TextView) layout.getChildAt(4)).setTextColor(0XffF85959);
                break;
            case 40:
                ((TextView) layout.getChildAt(5)).setTextColor(0XffF85959);
                break;
        }

        speedPopWindow = new PopupWindow(layout, JZUtils.dip2px(jzvdContext, 180), LayoutParams.MATCH_PARENT, true);
        speedPopWindow.setContentView(layout);
        speedPopWindow.setAnimationStyle(R.style.pop_animation);
        speedPopWindow.showAtLocation(textureViewContainer, Gravity.END, 0, 0);

        speedPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                JZUtils.hideSystemUI(getContext());
            }
        });
    }

    protected void clickBackTiny() {
        clearFloatScreen();
    }

    protected void clickBack() {
        if(isStaticFull)
        {
            try
            {
                ((Activity)getContext()).finish();
            }catch (Exception e)
            {
            }
            return;
        }
        backPress();
    }

    protected void clickSurfaceContainer() {
        startDismissControlViewTimer();
    }

    protected void clickPoster() {
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            Toast.makeText(jzvdContext, getResources().getString(R.string.no_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (state == STATE_NORMAL) {
            if (!jzDataSource.getCurrentUrl().toString().startsWith("file") &&
                    !jzDataSource.getCurrentUrl().toString().startsWith("/") &&
                    !JZUtils.isWifiConnected(jzvdContext) && !WIFI_TIP_DIALOG_SHOWED) {
                showWifiDialog();
                return;
            }
            startVideo();
        } else if (state == STATE_AUTO_COMPLETE) {
            onClickUiToggle();
        }
    }

    @Override
    public void setScreenNormal() {
        super.setScreenNormal();
        fullscreenButton.setImageResource(R.drawable.jz_enlarge);
        backButton.setVisibility(View.GONE);
        tinyBackImageView.setVisibility(View.INVISIBLE);
        changeStartButtonSize((int) getResources().getDimension(R.dimen.jz_start_button_w_h_normal));
        batteryTimeLayout.setVisibility(View.GONE);
        clarity.setVisibility(View.GONE);
        textScale.setVisibility(View.GONE);
        textSpeed.setVisibility(View.GONE);
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        //进入全屏之后要保证原来的播放状态和ui状态不变，改变个别的ui
        fullscreenButton.setImageResource(R.drawable.jz_shrink);
        backButton.setVisibility(View.VISIBLE);
        tinyBackImageView.setVisibility(View.INVISIBLE);
        batteryTimeLayout.setVisibility(View.VISIBLE);
        if (jzDataSource.urlsMap.size() == 1) {
            clarity.setVisibility(GONE);
        } else {
            clarity.setText(jzDataSource.getCurrentKey().toString());
            clarity.setVisibility(View.VISIBLE);
        }

        if(canScale)
        {
            textScale.setVisibility(View.VISIBLE);
            textScale.setText(getScaleName());
        }else
        {
            textScale.setVisibility(View.GONE);
        }

        if(canSpeed)
        {
            textSpeed.setVisibility(View.VISIBLE);
            textSpeed.setText(speed + "倍");
        }else
        {

            textSpeed.setVisibility(View.GONE);
        }

        changeStartButtonSize((int) getResources().getDimension(R.dimen.jz_start_button_w_h_fullscreen));
        setSystemTimeAndBattery();
    }

    @Override
    public void setScreenTiny() {
        super.setScreenTiny();
        tinyBackImageView.setVisibility(View.VISIBLE);
        setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        batteryTimeLayout.setVisibility(View.GONE);
        clarity.setVisibility(View.GONE);
    }

    @Override
    public void showWifiDialog() {
        super.showWifiDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(jzvdContext);
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), (dialog, which) -> {
            dialog.dismiss();
            WIFI_TIP_DIALOG_SHOWED = true;
            if (state == STATE_PAUSE) {
                startButton.performClick();
            } else {
                startVideo();
            }

        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), (dialog, which) -> {
            dialog.dismiss();
            releaseAllVideos();
            clearFloatScreen();
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                releaseAllVideos();
                clearFloatScreen();
            }
        });

        builder.create().show();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        super.onStartTrackingTouch(seekBar);
        cancelDismissControlViewTimer();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        startDismissControlViewTimer();
    }

    public void onClickUiToggle() {//这是事件
        if (bottomContainer.getVisibility() != View.VISIBLE) {
            setSystemTimeAndBattery();
            clarity.setText(jzDataSource.getCurrentKey().toString());
            textScale.setText(getScaleName());
        }
        if (state == STATE_PREPARING) {
            changeUiToPreparing();
            if (bottomContainer.getVisibility() == View.VISIBLE) {
            } else {
                setSystemTimeAndBattery();
            }
        } else if (state == STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
                changeUiToPlayingShow();
            }
        }else if (state == STATE_PAUSE)
        {
            if (bottomContainer.getVisibility() == View.VISIBLE)
            {
                changeUiToPauseClear();
            }
            else
            {
                changeUiToPauseShow();
            }
        }
//        else if (state == STATE_PAUSE) {
//            if (bottomContainer.getVisibility() == View.VISIBLE) {
//                changeUiToPauseClear();
//            } else {
//                changeUiToPauseShow();
//            }
//        }
    }

    public void setSystemTimeAndBattery() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        videoCurrentTime.setText(dateFormater.format(date));
        if ((System.currentTimeMillis() - LAST_GET_BATTERYLEVEL_TIME) > 30000) {
            LAST_GET_BATTERYLEVEL_TIME = System.currentTimeMillis();
            jzvdContext.registerReceiver(
                    battertReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            );
        } else {
            setBatteryLevel();
        }
    }

    public void setBatteryLevel() {
        int percent = LAST_GET_BATTERYLEVEL_PERCENT;
        if (percent < 15) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_10);
        } else if (percent >= 15 && percent < 40) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_30);
        } else if (percent >= 40 && percent < 60) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_50);
        } else if (percent >= 60 && percent < 80) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_70);
        } else if (percent >= 80 && percent < 95) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_90);
        } else if (percent >= 95 && percent <= 100) {
            batteryLevel.setBackgroundResource(R.drawable.jz_battery_level_100);
        }
    }

    //** 和onClickUiToggle重复，要干掉
    public void onCLickUiToggleToClear() {
        if (state == STATE_PREPARING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPreparing();
            } else {
            }
        } else if (state == STATE_PLAYING) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPlayingClear();
            } else {
            }
        } else if (state == STATE_PAUSE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToPauseClear();
            } else {
            }
        } else if (state == STATE_AUTO_COMPLETE) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                changeUiToComplete();
            } else {
            }
        }
    }

    @Override
    public void onProgress(int progress, long position, long duration) {
        super.onProgress(progress, position, duration);
//        if (progress != 0) bottomProgressBar.setProgress(progress);
    }

    @Override
    public void setBufferProgress(int bufferProgress) {
        super.setBufferProgress(bufferProgress);
//        if (bufferProgress != 0) bottomProgressBar.setSecondaryProgress(bufferProgress);
    }

    @Override
    public void resetProgressAndTime() {
        super.resetProgressAndTime();
//        bottomProgressBar.setProgress(0);
//        bottomProgressBar.setSecondaryProgress(0);
    }

    public void changeUiToNormal() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPreparing() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUIToPreparingPlaying() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUIToPreparingChangeUrl() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPlayingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToPlayingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToPauseShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToPauseClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToComplete() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToError() {
        switch (screen) {
            case SCREEN_NORMAL:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.INVISIBLE, View.VISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.VISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }

    }

    public void changeUiToLoadingShow() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                updateStartImage();
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void changeUiToLoadingClear() {
        switch (screen) {
            case SCREEN_NORMAL:
            case SCREEN_FULLSCREEN:
                setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.VISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
                break;
            case SCREEN_TINY:
                break;
        }
    }

    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro,
                                        int posterImg, int bottomPro, int retryLayout) {
        topContainer.setVisibility(topCon);
        bottomContainer.setVisibility(bottomCon);
        startButton.setVisibility(startBtn);
        loadingProgressBar.setVisibility(loadingPro);
        posterImageView.setVisibility(posterImg);
//        bottomProgressBar.setVisibility(bottomPro);
        mRetryLayout.setVisibility(retryLayout);
    }

    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_click_pause_selector);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            startButton.setImageResource(R.drawable.jz_click_replay_selector);
            replayTextView.setVisibility(VISIBLE);
        } else {
            startButton.setImageResource(R.drawable.jz_click_play_selector);
            replayTextView.setVisibility(GONE);
        }
    }

    @Override
    public void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(jzvdContext).inflate(R.layout.jz_dialog_progress, null);
            mDialogProgressBar = localView.findViewById(R.id.duration_progressbar);
            mDialogSeekTime = localView.findViewById(R.id.tv_current);
            mDialogTotalTime = localView.findViewById(R.id.tv_duration);
            mDialogIcon = localView.findViewById(R.id.duration_image_tip);
            mProgressDialog = createDialogWithView(localView);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : (int) (seekTimePosition * 100 / totalTimeDuration));
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.jz_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.jz_backward_icon);
        }
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissProgressDialog() {
        super.dismissProgressDialog();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showVolumeDialog(float deltaY, int volumePercent) {
        super.showVolumeDialog(deltaY, volumePercent);
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(jzvdContext).inflate(R.layout.jz_dialog_volume, null);
            mDialogVolumeImageView = localView.findViewById(R.id.volume_image_tip);
            mDialogVolumeTextView = localView.findViewById(R.id.tv_volume);
            mDialogVolumeProgressBar = localView.findViewById(R.id.volume_progressbar);
            mVolumeDialog = createDialogWithView(localView);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }
        if (volumePercent <= 0) {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.jz_close_volume);
        } else {
            mDialogVolumeImageView.setBackgroundResource(R.drawable.jz_add_volume);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        mDialogVolumeTextView.setText(volumePercent + "%");
        mDialogVolumeProgressBar.setProgress(volumePercent);
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissVolumeDialog() {
        super.dismissVolumeDialog();
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }

    @Override
    public void showBrightnessDialog(int brightnessPercent) {
        super.showBrightnessDialog(brightnessPercent);
        if (mBrightnessDialog == null) {
            View localView = LayoutInflater.from(jzvdContext).inflate(R.layout.jz_dialog_brightness, null);
            mDialogBrightnessTextView = localView.findViewById(R.id.tv_brightness);
            mDialogBrightnessProgressBar = localView.findViewById(R.id.brightness_progressbar);
            mBrightnessDialog = createDialogWithView(localView);
        }
        if (!mBrightnessDialog.isShowing()) {
            mBrightnessDialog.show();
        }
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        mDialogBrightnessTextView.setText(brightnessPercent + "%");
        mDialogBrightnessProgressBar.setProgress(brightnessPercent);
        onCLickUiToggleToClear();
    }

    @Override
    public void dismissBrightnessDialog() {
        super.dismissBrightnessDialog();
        if (mBrightnessDialog != null) {
            mBrightnessDialog.dismiss();
        }
    }

    public Dialog createDialogWithView(View localView) {
        Dialog dialog = new Dialog(jzvdContext, R.style.jz_style_dialog_progress);
        dialog.setContentView(localView);
        Window window = dialog.getWindow();
        window.addFlags(Window.FEATURE_ACTION_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.setLayout(-2, -2);
        WindowManager.LayoutParams localLayoutParams = window.getAttributes();
        localLayoutParams.gravity = Gravity.CENTER;
        window.setAttributes(localLayoutParams);
        return dialog;
    }

    public void startDismissControlViewTimer() {
        cancelDismissControlViewTimer();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, AUTO_TIME);
    }

    public void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }

    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        cancelDismissControlViewTimer();
    }

    @Override
    public void reset() {
        super.reset();
        cancelDismissControlViewTimer();
        unregisterWifiListener(getApplicationContext());
    }

    public void dissmissControlView() {
        if (state != STATE_NORMAL
                && state != STATE_ERROR
                && state != STATE_AUTO_COMPLETE) {
            post(() -> {
                bottomContainer.setVisibility(View.INVISIBLE);
                topContainer.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);

//                if (screen != SCREEN_TINY) {
//                    bottomProgressBar.setVisibility(View.VISIBLE);
//                }
                cancelProgressTimer();
            });
        }
    }

    public void registerWifiListener(Context context) {
        if (context == null) return;
        mIsWifi = JZUtils.isWifiConnected(context);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(wifiReceiver, intentFilter);
    }

    public void unregisterWifiListener(Context context) {
        if (context == null) return;
        try {
            context.unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public class DismissControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            dissmissControlView();
        }
    }

    public void setCanScale(boolean enableScale)
    {
        canScale = enableScale;
    }

    public void setCanSpeed(boolean enableSpeed)
    {
        canSpeed = enableSpeed;
    }
}
