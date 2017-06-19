package com.mx.demo.eye;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorInt;

/**
 * Created by mx on 6/15.
 */

public class SettingUtils {

    private static final String SETTING = "setting_config";
    private static final String SETTING_SW_COLOR = "setting_sw";
    private static final String SETTING_ALPHA = "setting_alpha";
    private static final String SETTING_SW_PROGRESS = "setting_sw_progress";
    private static final String SETTING_CLOSE_OPNE = "setting_close_opne";


    public static void saveSwColor(@ColorInt int sw) {
        saveSetting(SETTING_SW_COLOR,sw);
    }

    public static void saveAlpha(float alpha) {
        saveSetting(SETTING_ALPHA,alpha);
    }
    public static void saveSwColorProgress(float progress) {
        saveSetting(SETTING_SW_PROGRESS,progress);
    }

    public  static int getSwColor() {
        return getIntSetting(SETTING_SW_COLOR, APP.getApplication().getColor(R.color.transparent_dark));
    }

    public static float getAlpha() {
        return getFloatSetting(SETTING_ALPHA,0.4f);
    }
    public static int getSwColorProgress() {
        return getIntSetting(SETTING_SW_PROGRESS, 50);
    }

    public static void savePause(boolean pause) {
        saveSetting(SETTING_CLOSE_OPNE,pause);
    }
    public static boolean isPause() {
       return getBooleanSetting(SETTING_CLOSE_OPNE,false);
    }
    private static boolean getBooleanSetting(String name,boolean fal) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getBoolean(name, fal);
        } catch (Exception e) {
            return fal;
        }
    }
    private static void saveSetting(String name, boolean state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, state);
        editor.apply();
    }



    private static void saveSetting(String name, int state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(name, state);
        editor.apply();
    }

    private static void saveSetting(String name, float state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(name, state);
        editor.apply();
    }
    private static float getFloatSetting(String name, float defaultAlpha) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getFloat(name, defaultAlpha);
        } catch (Exception e) {
            return defaultAlpha;
        }
    }

    private static int getIntSetting(String name,int defaultColor) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getInt(name, defaultColor);
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
