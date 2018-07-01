package com.czc.blackblub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;

import com.czc.blackblub.util.StatusBarUtil;

public class SplashResultActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setStatusBar(this, R.color.color_val_FF212326);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_result_activity);

        findViewById(R.id.back_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMain();
            }
        });

    }

    void startMain() {
        startActivity(new Intent(SplashResultActivity.this, MainActivity.class));
        finish();
    }

    void onBack() {
        startMain();
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
