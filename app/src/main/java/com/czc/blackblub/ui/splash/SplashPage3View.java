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

public class SplashPage3View extends FrameLayout {

    private View clickView;

    public SplashPage3View(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SplashPage3View(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SplashPage3View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SplashPage3View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    void init(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.splasy_page3_view, this);

        clickView = rootView.findViewById(R.id.click_view);

    }


    public void setSplashClick(OnClickListener listener) {
        clickView.setOnClickListener(listener);
    }

}
