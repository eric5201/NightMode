package com.czc.blackblub.ui.splash;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.czc.blackblub.R;

public class SplashPageView extends FrameLayout {

    private ImageView bgIV;
    private View clickView;

    public SplashPageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SplashPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SplashPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SplashPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.splasy_page_view, this);

        bgIV = rootView.findViewById(R.id.bg_iv);
        clickView = rootView.findViewById(R.id.click_view);

    }

    public void setSplashBg(int res) {
        bgIV.setImageResource(res);
    }


    public void setSplashClick(OnClickListener listener) {
        clickView.setOnClickListener(listener);
    }

}
