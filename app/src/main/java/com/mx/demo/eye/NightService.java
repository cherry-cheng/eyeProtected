package com.mx.demo.eye;

import android.app.Notification;
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
import android.support.annotation.DrawableRes;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RemoteViews;

public class NightService extends Service {

    private WindowManager mWindowManager;
    private ImageView mImageView;
    private int mAdjustColor;
    private int mSwColor;
    private float mSwFractor;
    private float mAlpha;
    private MyReceiver mReceiver;
    private WindowManager.LayoutParams mLp;
    private RemoteViews mContentView;
    private Notification.Builder mBuilder;
    private boolean mIsPaused;
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

        mImageView = new ImageView(this);
        mImageView.setBackgroundColor(mAdjustColor);
        mImageView.setAlpha(mAlpha);

        mWindowManager.addView(mImageView, mLp);

        mIsPaused = false;
        startFront(R.drawable.ic_pause);
    }

    private void pause() {
        mWindowManager.removeView(mImageView);
        mImageView = null;

        mIsPaused = true;
        startFront(R.drawable.ic_start);
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mImageView == null) {
            mImageView = new ImageView(this);
            mImageView.setBackgroundColor(mAdjustColor);
            mImageView.setAlpha(mAlpha);
            int widthPixels = getResources().getDisplayMetrics().widthPixels;
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            int max = Math.max(widthPixels, heightPixels) + 500;
            mLp = new WindowManager.LayoutParams();
            mLp.width = max;
            mLp.height = max;
            mLp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            mLp.flags |= 0x738;
            mLp.format = PixelFormat.RGBA_8888;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mImageView, mLp);
            startFront(R.drawable.ic_pause);
        }
        return START_STICKY;
    }


    private void startFront(@DrawableRes int id) {

        if (mContentView == null) {
            Intent brPause = new Intent(Constans.ACTION_PAUSE);
            Intent brExit = new Intent(Constans.ACTION_EXIT);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent changeIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 2, brPause, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent exitIntent = PendingIntent.getBroadcast(this, 3, brExit, PendingIntent.FLAG_CANCEL_CURRENT);

            mContentView = new RemoteViews(getPackageName(), R.layout.notify_layout);
            mContentView.setOnClickPendingIntent(R.id.change, changeIntent);
            mContentView.setOnClickPendingIntent(R.id.pause, pauseIntent);
            mContentView.setOnClickPendingIntent(R.id.exit, exitIntent);
            mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_small_icon);
        }
        mContentView.setImageViewResource(R.id.pause, id);
        mBuilder.setContent(mContentView);
        startForeground(1, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mImageView);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        stopForeground(true);
        stopSelf();
        System.exit(0);
    }

    private float getSwFractor() {
        return mSwFractor;
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constans.ACTION_PAUSE)) {
                if (!mIsPaused) {
                    pause();
                } else {
                    restart();
                }
            } else if (intent.getAction().equals(Constans.ACTION_EXIT)) {
                stopForeground(true);
                stopSelf();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }
    }

}
