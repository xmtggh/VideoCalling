package com.ggh.video.utils;

import android.content.Context;
import androidx.annotation.NonNull;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

public class PermissionManager {


    public static void requestPermission(final Context context, final Callback callback, String... permissions) {
        AndPermission.with(context)
                .permission(permissions)
//                .rationale(mRationale)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        if (callback != null) {
                            callback.permissionSuccess();

                        }
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        //授权失败？是否弹出窗
                        if (callback != null)
                            callback.permissionFailed();
                        if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                        }
                    }
                })
                .start();
    }

    public interface Callback {
        void permissionSuccess();

        void permissionFailed();
    }
}
