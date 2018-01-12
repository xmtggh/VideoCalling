package com.vonchenchen.android_video_demos;

import android.content.Context;
import android.os.Environment;

/**
 * Created by lidechen on 6/2/17.
 */

public class FileUtil {

    public static final String APP_ROOT_DICT = "/android_video_demo";

    public static String getFilePath(Context context) {
        String path;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_ROOT_DICT;
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            path = context.getFilesDir().getAbsolutePath();
        }
        return path;
    }
}
