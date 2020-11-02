package cn.jzvd;

import java.io.Serializable;

public class VideoPlayConfig implements Serializable
{
    private String url = "";
    private String title = "";
    private float speed = 1f;
    private boolean isLoop;
    private boolean canScale = true;
    private boolean canSpeed = true;
    private boolean isLandscape;
    private boolean isSensor = true;
    private boolean isShowUI = true;

    public VideoPlayConfig(String url)
    {
        this.url = url;
    }

    public VideoPlayConfig setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public VideoPlayConfig setSpeed(float speed)
    {
        this.speed = speed;
        return this;
    }

    public VideoPlayConfig setLoop(boolean loop)
    {
        isLoop = loop;
        return this;
    }

    public VideoPlayConfig setCanScale(boolean canScale)
    {
        this.canScale = canScale;
        return this;
    }

    public VideoPlayConfig setCanSpeed(boolean canSpeed)
    {
        this.canSpeed = canSpeed;
        return this;
    }

    public VideoPlayConfig setLandscape(boolean landscape)
    {
        isLandscape = landscape;
        return this;
    }

    public VideoPlayConfig setSensor(boolean sensor)
    {
        isSensor = sensor;
        return this;
    }

    public VideoPlayConfig setShowUI(boolean showUI)
    {
        isShowUI = showUI;
        return this;
    }

    public String getUrl()
    {
        return url;
    }

    public String getTitle()
    {
        return title;
    }

    public float getSpeed()
    {
        return speed;
    }

    public boolean isLoop()
    {
        return isLoop;
    }

    public boolean isCanScale()
    {
        return canScale;
    }

    public boolean isCanSpeed()
    {
        return canSpeed;
    }

    public boolean isLandscape()
    {
        return isLandscape;
    }

    public boolean isSensor()
    {
        return isSensor;
    }

    public boolean isShowUI()
    {
        return isShowUI;
    }
}
