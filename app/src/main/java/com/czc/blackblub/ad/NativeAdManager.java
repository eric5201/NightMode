package com.czc.blackblub.ad;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czc.blackblub.R;
import com.czc.blackblub.util.Utility;
import com.czc.blackblub.widget.MediaView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * $CONTENT
 *
 * @author eric.cai  04.02 2018
 */

public class NativeAdManager {

    private static NativeAdManager instance = Hold.hold;
    private SharedPreferences sp;
    private final String SHOW_TIME = "show_time";
    private LinkedList<NativeAd> adList = new LinkedList();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Context mContext;
    private int showCound = 0;
    private OnInflatedAdListener inflatedAdListener;

    public static NativeAdManager getInstance() {
        return instance;
    }

    private NativeAdManager() {

    }

    public void init(Context context) {
        this.mContext = context;
        sp = context.getSharedPreferences("native_ad", Context.MODE_PRIVATE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (Utility.canDrawOverlays(mContext)) {
//                    showAd("INIT_AD_MANAGER");
//                } else {
                loadAd();
//                }
            }
        }, 500);
    }

    public void setOnInflatedAdListener(OnInflatedAdListener listener) {
        this.inflatedAdListener = listener;
    }


    public void showAd(String from) {
        Log.d("eric", "[showAd] from is: " + from);
        showAd(false);
    }

    private void showAd(boolean isRetry) {
        if ((System.currentTimeMillis() - sp.getLong(SHOW_TIME, 0l)) <= 10000) {
            return;
        }
        Log.d("eric", "[showAd] -> showCound:" + showCound);
        if (adList.size() > 0) {
            showCound = 0;
            handler.postDelayed(showAdRunnable, 200);
            return;
        } else if (adList.size() <= 0) {
            if (!isRetry) {
                showCound++;
            }
            loadAd();
        }
    }

    private Runnable showAdRunnable = new Runnable() {
        @Override
        public void run() {
            showCound = 0;
            if ((System.currentTimeMillis() - sp.getLong(SHOW_TIME, 0l)) <= 10000) {
                return;
            }
            sp.edit().putLong(SHOW_TIME, System.currentTimeMillis()).apply();
            Log.d("eric", "[showAd] -> show time:" + sp.getLong(SHOW_TIME, 0l));
            inflateAd(adList.pollFirst());

            if (adList.size() < 1) {
                loadAd();
            }
        }
    };

    private void loadAd() {
        if (adList.size() >= 1) {
            return;
        }
        //174967429815219_174971403148155 / YOUR_PLACEMENT_ID
        NativeAd nativeAd = new NativeAd(mContext, "174967429815219_174971403148155");
        nativeAd.setAdListener(getAdListener(nativeAd));
        // Request an ad
        nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }

    private NativeAdListener getAdListener(final NativeAd nativeAd) {
        return new NativeAdListener() {

            @Override
            public void onMediaDownloaded(Ad ad) {
                Log.e("eric", "[onMediaDownloaded] -> Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError error) {
                Log.d("eric", "[onError]");
                // Ad error callback
                if (adList.size() < 1) {
                    loadAd();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d("eric", "[onAdLoaded]");
                // Ad loaded callback
                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }

                Log.d("eric", "[onAdLoaded] -> showCound:" + showCound);
                adList.add(nativeAd);
                if (showCound > 0) {
                    showCound = 0;
                    showAd(true);
                }

            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d("eric", "[onAdClicked]");
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d("eric", "[onLoggingImpression]");
                // Ad impression logged callback
            }
        };
    }

    private void inflateAd(NativeAd nativeAd) {
        // Add the Ad view into the ad container.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_4_99_layout, null);

        // Add the AdChoices icon
        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdChoicesView adChoicesView = new AdChoicesView(mContext, nativeAd, true);
        adChoicesContainer.addView(adChoicesView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);

        if (null == inflatedAdListener || !inflatedAdListener.onInflatedAd(adView)) {
            new NativeAdDialog(mContext).showAd(adView);
        }
    }

    public interface OnInflatedAdListener {
        boolean onInflatedAd(View adView);
    }

    static final class Hold {
        final static NativeAdManager hold = new NativeAdManager();
    }
}
