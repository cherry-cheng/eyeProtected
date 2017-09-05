package com.mx.demo.eye;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {


    private static final int OVERLAY_PERMISSION_REQ_CODE = 111;
    private static String TAG = "MainActivity";
    private Intent mService;
    private SeekBar mAlphaSeekbar;
    private ServiceConnection mCon;
    private Messenger mMessenger;
    private CheckBox mCloseCB;

    private CheckBox mDrawDownCB;
    private RadioGroup swChoose;
    private SeekBar swSeekBar;
    private RelativeLayout belowView;
    private View mSetting;
    private MyReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_view_layout);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = getWindowManager().getDefaultDisplay().getWidth();
        getWindow().setAttributes(lp);

        initView();
        initProp();

        mService = new Intent(this, NightService.class);
        bindAndStartServiceIfNeed();
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.ACTION_PAUSE);
        registerReceiver(mReceiver, filter);
    }



    private void showNeedPermissionsMessage() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Toast.makeText(this, "请授予必要的权限", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName()));
                    startActivity(intent);
                }
            }, 500);

        }
    }





    @Override
    protected void onStart() {
        super.onStart();
    }

    private void syncState() {
        if (SettingUtils.isPause()) {
            mCloseCB.setChecked(false);
        } else {
            mCloseCB.setChecked(true);
        }
        int colorID = SettingUtils.getSwColorID();
        swChoose.check(colorID);

        float alpha = SettingUtils.getAlpha();
        mAlphaSeekbar.setProgress((int) (alpha * mAlphaSeekbar.getMax()));

        int progress = SettingUtils.getSwColorProgress();
        swSeekBar.setProgress(swSeekBar.getMax() - progress * swSeekBar.getMax());
    }

    private void initProp() {
        swSeekBar.setMax(220);
        swSeekBar.setOnSeekBarChangeListener(this);

        mAlphaSeekbar.setMax(220);
        mAlphaSeekbar.setOnSeekBarChangeListener(this);

        swChoose.setOnCheckedChangeListener(this);

        mCloseCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingUtils.savePause(!isChecked);
                sendMessageToServer(!isChecked ? Constans.PAUSE : Constans.RESTART, 0);
            }
        });

        mDrawDownCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                belowView.setVisibility(mDrawDownCB.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "setting are not support", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCon != null) {
            unbindService(mCon);
            mCon = null;
        }
        if (mService != null) {
            unregisterReceiver(mReceiver);
            mService = null;
        }
    }

    private void initView() {
        mCloseCB = (CheckBox) findViewById(R.id.close_cb);
        mDrawDownCB = (CheckBox) findViewById(R.id.drawer_down);
        mAlphaSeekbar = (SeekBar) findViewById(R.id.alpha_seekBar);
        swChoose = (RadioGroup) findViewById(R.id.sw_choose);
        swSeekBar = (SeekBar) findViewById(R.id.sw_seekBar);
        belowView = (RelativeLayout) findViewById(R.id.below_view);
        mSetting = findViewById(R.id.setting);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == swSeekBar) {
            sendMessageToServer(Constans.CHANGE_SW, seekBar.getMax() - progress);
        } else if (seekBar == mAlphaSeekbar) {
            mCloseCB.setChecked(progress >= 0);
            sendMessageToServer(Constans.CHANGE_LIGHT, progress);
        }
    }

    private void sendMessageToServer(int what, int arg1) {
        if (mMessenger != null) {
            Message message = new Message();
            message.what = what;
            message.replyTo = mMessenger;
            message.arg1 = arg1;
            try {
                mMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        syncState();
        showNeedPermissionsMessage();
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        bindAndStartServiceIfNeed();
    }

    private void bindAndStartServiceIfNeed() {
        if (null == mCon) {
            mCon = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mMessenger = new Messenger(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            bindService(mService, mCon, BIND_AUTO_CREATE);
            startService(mService);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        bindAndStartServiceIfNeed();
        sendMessageToServer(Constans.SET_SW, checkedId);
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constans.ACTION_PAUSE)) {
                mCloseCB.setChecked(!mCloseCB.isChecked());
            }
        }
    }

}
