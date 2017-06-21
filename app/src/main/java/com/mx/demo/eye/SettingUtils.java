package com.mx.demo.eye;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorInt;

/**
 * Created by mx on 6/15.
 */

class SettingUtils {

    private static final String SETTING = "setting_config";
    private static final String SETTING_SW_COLOR = "setting_sw";
    private static final String SETTING_SW_ID = "setting_sw_id";
    private static final String SETTING_ALPHA = "setting_alpha";
    private static final String SETTING_SW_PROGRESS = "setting_sw_progress";
    private static final String SETTING_CLOSE_OPNE = "setting_close_opne";


    public static void saveSwColor(@ColorInt int sw) {
        saveIntSetting(SETTING_SW_COLOR, sw);
    }



    public static void saveAlpha(float alpha) {
        saveFloatSetting(SETTING_ALPHA, alpha);
    }

    public static void saveSwColorProgress(float progress) {
        saveFloatSetting(SETTING_SW_PROGRESS, progress);
    }

    public static int getSwColor() {
        return getIntSetting(SETTING_SW_COLOR, APP.getApplication().getColor(R.color.transparent_dark));
    }

    public static float getAlpha() {
        return getFloatSetting(SETTING_ALPHA,0.4f);
    }

    public static int getSwColorProgress() {
        return getIntSetting(SETTING_SW_PROGRESS, 50);
    }

    public static void savePause(boolean pause) {
        saveBooleanSetting(SETTING_CLOSE_OPNE,pause);
    }

    public static boolean isPause() {
        return getBooleanSetting(SETTING_CLOSE_OPNE, false);
    }

    private static boolean getBooleanSetting(String name,boolean def) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getBoolean(name, def);
        } catch (Exception e) {
            return false;
        }
    }

    private static void saveBooleanSetting(String name,boolean state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, state);
        editor.commit();
    }
    private static void saveFloatSetting(String name, float state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(name, state);
        editor.commit();
    }

    private static void saveIntSetting(String name, int state) {
        SharedPreferences sp = APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(name, state);
        editor.commit();
    }

    private static float getFloatSetting(String name,float def) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getFloat(name, def);
        } catch (Exception e) {
            return 0.4f;
        }
    }

    private static int getIntSetting(String name, int defaultColor) {
        try {
            return APP.getApplication().getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                    .getInt(name, defaultColor);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public static void saveSwColorID(int sw) {
        saveIntSetting(SETTING_SW_ID, sw);
    }

    public static int getSwColorID() {
        return getIntSetting(SETTING_SW_ID, R.id.rb_0);
    }


}
