package com.czc.blackblub.ad;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.czc.blackblub.R;

/**
 * $CONTENT
 *
 * @author eric.cai  06.07 2018
 */
public class NativeAdWindow {

    private static NativeAdWindow instance;
    private WindowManager mWindow;
    private WindowManager.LayoutParams lp;


    public static final NativeAdWindow getInstance() {
        if (null == instance) {
            instance = new NativeAdWindow();
        }
        return instance;
    }

    private NativeAdWindow() {

    }

    private WindowManager getWindow(Context context) {
        if (null == mWindow) {
             mWindow = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindow;
    }

    private WindowManager.LayoutParams getLp() {
        if (null == lp) {
            lp = new WindowManager.LayoutParams();
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
            lp.format = PixelFormat.RGBA_8888;
            lp.gravity = Gravity.CENTER;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT; //长
            lp.height = WindowManager.LayoutParams.MATCH_PARENT; //宽
            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE|ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        }
        return lp;
    }

    public void showAd(final Context context, final View adContentView){
        final LinearLayout containerView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.native_dialog_layout, null);
        ((LinearLayout)containerView.findViewById(R.id.native_ad_container)).addView(adContentView);
        containerView.findViewById(R.id.netive_ad_exit_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow(context).removeViewImmediate(adContentView);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getWindow(context).addView(containerView, getLp());
            }
        }, 0);
    }
}
