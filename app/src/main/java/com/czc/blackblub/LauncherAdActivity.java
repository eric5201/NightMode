package com.czc.blackblub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.czc.blackblub.ad.NativeAdManager;
import com.czc.blackblub.util.SharePreUtil;
import com.czc.blackblub.util.StatusBarUtil;

public class LauncherAdActivity extends Activity implements View.OnClickListener {

    private View adView;
    private View closeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBar(this, R.color.color_val_FF212326);

        super.onCreate(savedInstanceState);

        if (SharePreUtil.isFirstStartApp(this)) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
            return;
        }
        if (!SharePreUtil.isReboot()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.launcher_ad_activity);

        adView = findViewById(R.id.ad_layout);
        closeView = findViewById(R.id.close_layout);

        adView.setOnClickListener(this);
        closeView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == adView.getId()) {
            NativeAdManager.getInstance().showAd("splash_ad_activity");
        } else if (v.getId() == closeView.getId()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
