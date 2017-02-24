package com.android.example.popularmovie.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by hp on 2017/2/25.
 */

public class DisplayUtil {

    public static int getScreenWidth(Activity activity){
        //获取屏幕宽度
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
