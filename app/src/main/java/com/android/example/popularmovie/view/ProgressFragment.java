package com.android.example.popularmovie.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by hp on 2017/2/26.
 */

public class ProgressFragment extends DialogFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //setStyle(DialogFragment.STYLE_NO_TITLE,0); 加上这句话报错, 看来我写的有点儿问题
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return ProgressDialog.show(getActivity(), "", "正在加载电影，请稍等...");
    }

}
