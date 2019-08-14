package com.ggh.video;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ggh.video.utils.CheckPermissionUtils;
import com.ggh.video.utils.NetUtils;
import com.ggh.video.utils.PermissionManager;
import com.yanzhenjie.permission.Permission;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.ed_ip)
    EditText edIp;
    @BindView(R.id.ed_port)
    EditText edPort;
    @BindView(R.id.ed_localport)
    EditText edLocalport;
    @BindView(R.id.tv_ip)
    TextView tvIp;
    private String targetport;
    private String targetIp;
    private String localPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initPermission();
        tvIp.setText("本地ip地址为"+NetUtils.getIPAddress(MainActivity.this));

    }


    /**
     * 初始化权限事件
     */
    private void initPermission() {
        //检查权限
        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
            @Override
            public void permissionSuccess() {
                PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                    @Override
                    public void permissionSuccess() {
                        PermissionManager.requestPermission(MainActivity.this, new PermissionManager.Callback() {
                            @Override
                            public void permissionSuccess() {

                            }

                            @Override
                            public void permissionFailed() {

                            }
                        }, Permission.Group.STORAGE);
                    }

                    @Override
                    public void permissionFailed() {

                    }
                }, Permission.Group.MICROPHONE);
            }

            @Override
            public void permissionFailed() {

            }
        }, Permission.Group.CAMERA);

    }

    @OnClick(R.id.start)
    public void onViewClicked() {
        targetIp = edIp.getText().toString();
        targetport = edPort.getText().toString();
        localPort = edLocalport.getText().toString();
        if (TextUtils.isEmpty(targetIp)) {
            Toast.makeText(MainActivity.this, "目标ip不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(targetport)) {
            Toast.makeText(MainActivity.this, "目标端口不能为空", Toast.LENGTH_SHORT).show();
            return;

        }
        if (TextUtils.isEmpty(localPort)) {
            Toast.makeText(MainActivity.this, "本地不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        VideoTalkActivity.newInstance(MainActivity.this, targetIp, targetport, localPort);
    }
}
