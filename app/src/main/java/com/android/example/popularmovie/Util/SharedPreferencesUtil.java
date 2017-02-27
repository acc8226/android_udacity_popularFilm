package com.android.example.popularmovie.Util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hp on 2017/2/27.
 */

public final class SharedPreferencesUtil {

    private SharedPreferencesUtil() {
    }

    private static final String MOVIE_DATA = "film_data";
    private static final String MOVIE_SORT_KEY = "film_sort";

    /**
     * 获取存储在本地的电影排序
     * @param context
     * @return
     */
    public static int getMovieSort(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MOVIE_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(MOVIE_SORT_KEY, 0);
    }

    /**
     * 将电影排序的值存储在本地文件上
     * @param context
     * @param value
     */
    public static void putMovieSort(final Context context, final int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MOVIE_DATA, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(MOVIE_SORT_KEY, value)
                .apply();
    }
}
