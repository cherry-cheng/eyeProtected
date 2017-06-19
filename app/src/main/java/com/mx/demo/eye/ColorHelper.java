package com.mx.demo.eye;

import android.graphics.Color;


class ColorHelper {


    public static int adjustColor(int color,float fractor) {
        //get current fractor
        int red = (int) (Color.red(color) * fractor);
        int blue = (int) (Color.blue(color) * fractor);
        int green = (int) (Color.green(color) * fractor);
        return Color.rgb(red, green, blue);
    }



}
