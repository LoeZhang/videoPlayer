package cn.jzvd;

import android.content.Context;
import android.content.Intent;

public class LoeVideoPlayer
{
    public static void play(Context context, String url)
    {
        context.startActivity(new Intent(context, LoeVideoActivity.class)
                .putExtra("url", url));
    }

    public static void play(Context context, String url, String title, boolean isLandscape)
    {
        context.startActivity(new Intent(context, LoeVideoActivity.class)
                .putExtra("config", new VideoPlayConfig(url)
                        .setTitle(title)
                        .setLandscape(isLandscape)));
    }

    public static void play(Context context, VideoPlayConfig config)
    {
        context.startActivity(new Intent(context, LoeVideoActivity.class)
                .putExtra("config", config));
    }
}
