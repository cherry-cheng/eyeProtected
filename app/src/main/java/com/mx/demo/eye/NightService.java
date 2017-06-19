package com.mx.demo.eye;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NightService extends Service {

    private boolean mPause = false;
    private WindowManager mWindowManager;
    private ImageView mImageView;
    private int mAdjustColor;
    private int mSwColor;
    private float mSwFractor;
    private float mAlpha;
    private MyReceiver mReceiver;
    private WindowManager.LayoutParams mLp;
    private final Messenger mMessenger = new Messenger(new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constans.SET_SW:
                    setSwColor(msg.arg1);
                    break;
                case Constans.CHANGE_LIGHT:
                    handleAlpha(msg.arg1);
                    break;
                case Constans.CHANGE_SW:
                    handleSwProgress(msg.arg1);
                    break;
                case Constans.PAUSE:
                    pause();
                    break;
                case Constans.RESTART:
                    restart();
                    break;
            }
        }
    });

    public NightService() {
        mSwColor = SettingUtils.getSwColor();
        mSwFractor = SettingUtils.getSwColorProgress();
        mAlpha = SettingUtils.getAlpha();
        mAdjustColor = ColorHelper.adjustColor(mSwColor, mSwFractor);
    }

    private void restart() {
        mPause = false;
        if (mImageView != null) {
            mWindowManager.addView(mImageView, mLp);
        }
        startFront(R.drawable.ic_pause);
        SettingUtils.savePause(false);
    }

    private void pause() {
        mPause = true;
        if (mImageView != null) {
            mWindowManager.removeView(mImageView);
        }
        startFront(R.drawable.ic_start);
        SettingUtils.savePause(true);
    }

    private void setSwColor(int sw) {
        SettingUtils.saveSwColorID(sw);
        switch (sw) {
            case R.id.rb_0:
                mSwColor = mAdjustColor = getColor(R.color.transparent_dark);
                break;
            case R.id.rb_2000:
                mSwColor = getColor(R.color.transparent_2000);
                break;
            case R.id.rb_2700:
                mSwColor = getColor(R.color.transparent_2700);
                break;
            case R.id.rb_3200:
                mSwColor = getColor(R.color.transparent_3200);
                break;
            case R.id.rb_3400:
                mSwColor = getColor(R.color.transparent_3400);
                break;
        }
        mAdjustColor = ColorHelper.adjustColor(mSwColor, getSwFractor());
        mImageView.setBackgroundColor(mAdjustColor);
        SettingUtils.saveSwColor(mSwColor);
    }

    private void handleSwProgress(int sw) {
        mSwFractor = (float) sw / 255.0f;
        //调整暗度
        mAdjustColor = ColorHelper.adjustColor(mSwColor, mSwFractor);
        mImageView.setBackgroundColor(mAdjustColor);
        SettingUtils.saveSwColorProgress(mSwFractor);
    }

    private void handleAlpha(int light) {
        mAlpha = (float) light / 255.0f;
        mImageView.setAlpha(mAlpha);
        SettingUtils.saveAlpha(mAlpha);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.ACTION_EXIT);
        filter.addAction(Constans.ACTION_PAUSE);
        registerReceiver(mReceiver, filter);
        return mMessenger.getBinder();
    }
    private void clear() {
        mImageView = null;
        mLp = null;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(1);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clear();
        if (mImageView == null) {
            mImageView = new ImageView(this);
            mImageView.setBackgroundColor(mAdjustColor);
            mImageView.setAlpha(mAlpha);
            int widthPixels = getResources().getDisplayMetrics().widthPixels;
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            mLp = new WindowManager.LayoutParams();
            mLp.width = widthPixels;
            mLp.height = heightPixels + 500;
            mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            mLp.flags |= 0x738;
            mLp.format = PixelFormat.RGBA_8888;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            startFront(R.drawable.ic_pause);
            mWindowManager.addView(mImageView, mLp);
        }
        return START_NOT_STICKY;
    }



    private void startFront(int id) {
        Intent brPause = new Intent(Constans.ACTION_PAUSE);
        Intent brExit = new Intent(Constans.ACTION_EXIT);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fromNotification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent changeIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 2, brPause, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent exitIntent = PendingIntent.getBroadcast(this, 3, brExit, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notify_layout);
        contentView.setOnClickPendingIntent(R.id.change, changeIntent);
        contentView.setOnClickPendingIntent(R.id.pause, pauseIntent);
        contentView.setOnClickPendingIntent(R.id.exit, exitIntent);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_small_icon)
                .setContent(contentView);
        contentView.setImageViewResource(R.id.pause, id);
        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mImageView);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private float getSwFractor() {
        return mSwFractor;
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constans.ACTION_PAUSE)) {
                if (!mPause) {
                    pause();
                } else {
                    restart();
                }
            } else if (intent.getAction().equals(Constans.ACTION_EXIT)) {
                System.exit(0);
            }
        }
    }

}
