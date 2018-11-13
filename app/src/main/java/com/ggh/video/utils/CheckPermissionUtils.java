package com.ggh.video.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apple on 2016/12/3.
 */

public final class CheckPermissionUtils {
    private CheckPermissionUtils() {
    }

    //需要申请的权限
    private static String[] permissions = new String[]{
            Manifest.permission_group.CAMERA,
            Manifest.permission_group.STORAGE,
            Manifest.permission_group.MICROPHONE
    };

    //检测权限
    public static String[] checkPermission(Context context){
        List<String> data = new ArrayList<>();//存储未申请的权限
        for (String permission : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(context, permission);
            if(checkSelfPermission == PackageManager.PERMISSION_DENIED){//未申请
                data.add(permission);
            }
        }
        return data.toArray(new String[data.size()]);
    }
}
