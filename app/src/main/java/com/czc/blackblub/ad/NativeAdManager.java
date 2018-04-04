package com.czc.blackblub.ad;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czc.blackblub.widget.MediaView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.czc.blackblub.R;

/**
 * $CONTENT
 *
 * @author eric.cai  04.02 2018
 */

public class NativeAdManager {

    private static NativeAdManager instance = Hold.hold;
    private LinkedList<NativeAd> adList = new LinkedList();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Context context;
    private int showCound = 0;
    private long showTime = 0;

    public static NativeAdManager getInstance() {
        return instance;
    }

    private NativeAdManager() {

    }

    public void init(Context context) {
        this.context = context;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAd();
            }
        }, 500);
    }


    public void showAd() {
        showAd(false);
    }

    private void showAd(boolean isRetry) {
        final long currTime = System.currentTimeMillis();
        if ((currTime - showTime) <= 10000) {
            return;
        }
        if (!isRetry) {
            showCound++;
        }
        Log.d("eric", "[showAd] -> showCound:" + showCound);
        if (adList.size() > 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showCound--;
                    showTime = currTime;
                    Log.d("eric", "[showAd] -> 222 showCound:" + showCound);
                    displayAdView(adList.pollFirst());

                    if (adList.size() < 1) {
                        loadAd();
                    }
                }
            });
            return;
        }
        loadAd();
    }

    private void loadAd() {
        //174967429815219_174971403148155 / YOUR_PLACEMENT_ID
        NativeAd nativeAd = new NativeAd(context, "174967429815219_174971403148155");
        nativeAd.setAdListener(getAdListener(nativeAd));
        // Request an ad
        nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }

    private AdListener getAdListener(final NativeAd nativeAd) {
        return new AdListener() {

            @Override
            public void onError(Ad ad, AdError error) {
                Log.d("eric", "[onError]");
                // Ad error callback
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
                    showAd(true);
                }

                if (adList.size() < 1) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadAd();
                        }
                    }, 100);
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

    private void displayAdView(NativeAd nativeAd) {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, null);

        // Create native UI using the ad metadata.
        ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = (TextView) adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = (MediaView) adView.findViewById(R.id.native_ad_media_view);
        TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = (TextView) adView.findViewById(R.id.native_ad_body);
        Button nativeAdCallToAction = (Button) adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdTitle());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdBody.setText(nativeAd.getAdBody());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());

        // Download and display the ad icon.
        NativeAd.Image adIcon = nativeAd.getAdIcon();
        NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

        // Download and display the cover image.
        nativeAdMedia.setNativeAd(nativeAd);

        // Add the AdChoices icon
        LinearLayout adChoicesContainer = (LinearLayout) adView.findViewById(R.id.ad_choices_container);
        AdChoicesView adChoicesView = new AdChoicesView(context, nativeAd, true);
        adChoicesContainer.addView(adChoicesView);

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeAd.registerViewForInteraction(adView, clickableViews);

        new NativeAdDialog(context).showAd(adView);
    }

    static final class Hold {
        final static NativeAdManager hold = new NativeAdManager();
    }
}
