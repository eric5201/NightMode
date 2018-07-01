package com.czc.blackblub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.czc.blackblub.ui.splash.SplashPage2View;
import com.czc.blackblub.ui.splash.SplashPage3View;
import com.czc.blackblub.ui.splash.SplashPageView;
import com.czc.blackblub.util.SharePreUtil;
import com.czc.blackblub.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity implements View.OnClickListener {

    private ViewPager viewPager;
    private List<View> views = new ArrayList<>();
//    private Integer[] images = new Integer[]{R.drawable.splash_one, R.drawable.splash_two, R.drawable.splash_three};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        StatusBarUtil.setStatusBar(this, R.color.color_val_FF212326);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        viewPager = findViewById(R.id.viewpager);

        initViewPager();
    }

    void initViewPager() {

        SplashPageView itemView = new SplashPageView(this);
        itemView.setSplashClick(this);
        views.add(itemView);

        SplashPage2View itemView2 = new SplashPage2View(this);
        itemView2.setSplashClick(this);
        views.add(itemView2);

        SplashPage3View itemView3 = new SplashPage3View(this);
        itemView3.setSplashClick(this);
        views.add(itemView3);

        viewPager.setAdapter(new SplashPagerAdapter());
        viewPager.setOffscreenPageLimit(views.size());

    }

    @Override
    public void onClick(View v) {
        if (viewPager.getCurrentItem() >= (views.size() - 1)) {
            SharePreUtil.saveFirstStartAppTag(this);
            startActivity(new Intent(this, SplashResultActivity.class));
            finish();
        } else {
            int index = viewPager.getCurrentItem() + 1;
            index = index >= views.size() ? (views.size() - 1) : index;
            viewPager.setCurrentItem(index);
        }
    }


    class SplashPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }
    }
}
