package com.czc.blackblub.ad;


import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.czc.blackblub.R;

/**
 * $CONTENT
 *
 * @author eric.cai  03.21 2018
 */

public class NativeAdDialog extends Dialog {

    private LinearLayout nativeAdContainer;
    private View exitView;
    private View adContentView;

    public NativeAdDialog(@NonNull Context context) {
        super(context, R.style.nativeAdDialog);

        setContentView(R.layout.native_dialog_layout);
        getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setCanceledOnTouchOutside(true);
        nativeAdContainer = findViewById(R.id.native_ad_container);
        exitView = findViewById(R.id.netive_ad_exit_tv);
        exitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (null != adContentView) {
            nativeAdContainer.addView(adContentView);
        }
    }

    public void showAd(final View adContentView) {
        this.adContentView = adContentView;
        super.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null != nativeAdContainer) {
                    nativeAdContainer.addView(adContentView);
                }
            }
        }, 0);
    }
}
