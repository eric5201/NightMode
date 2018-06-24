package com.czc.blackblub.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreUtil {

    private static boolean isReboot = true;

    private static final SharedPreferences getSp(Context context) {
        return context.getSharedPreferences("splash", Context.MODE_PRIVATE);
    }

    public static final boolean isFirstStartApp(Context context) {
        return getSp(context).getInt("splash_start_tag", 0) == 0;
    }

    public static final void saveFirstStartAppTag(Context context) {
        getSp(context).edit().putInt("splash_start_tag", 1).apply();
    }

    public static final boolean isReboot() {
        return isReboot;
    }

    public static final void saveBootTag() {
        isReboot = false;
    }
}
