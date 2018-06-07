package com.czc.blackblub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.czc.blackblub.app.Constants;
import com.czc.blackblub.receiver.ActionReceiver;
import com.czc.blackblub.ad.NativeAdManager;
import com.czc.blackblub.ui.SchedulerDialog;
import com.czc.blackblub.util.Utility;
import com.github.zagum.expandicon.ExpandIconView;

import com.czc.blackblub.service.MaskService;
import com.czc.blackblub.ui.adapter.ModeListAdapter;
import com.czc.blackblub.util.AlarmUtil;
import com.czc.blackblub.util.Settings;

public class MainActivity extends Activity {

    // Views & States
    private ImageButton mToggle;
    private Switch mSwitch;
    private SeekBar mSeekBar;
    private ExpandIconView mExpandIcon;
    private View mDivider, mMiniSchedulerBar;
    private TextView mMiniSchedulerStatus, mButtonTip;

    private View mSchedulerRow;
    private TextView mSchedulerStatus;
    private ImageView mSchedulerIcon;

    private View mDarkThemeRow;

    private View mAdvancedModeRow;
    private TextView mAdvancedModeText;

    private View mYellowFilterRow;
    private SeekBar mYellowFilterSeekBar;

    private AlertDialog mFirstRunDialog;

    private static boolean isExpand = false, hasDismissFirstRunDialog = false;

    // Service states
    private boolean isRunning = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMaskServiceInterface msi = IMaskServiceInterface.Stub.asInterface(service);
            try {
                setToggleIconState(isRunning = msi.isShowing());
                Utility.createStatusBarTiles(MainActivity.this, isRunning);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    // Settings
    private Settings mSettings;

    // Local broadcast receivers
    private MessageReceiver mReceiver;

    // Constants
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSettings = Settings.getInstance(this);

        // Apply theme and transparent system ui
        Utility.applyTransparentSystemUI(this);
        if (mSettings.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }

        // Apply Noto Sans CJK Full font from FontProvider API
        Utility.applyNotoSansCJK(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up rootView & aboutButton's click event
        findViewById(R.id.root_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_about).setVisibility(View.GONE);
//                .setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setView(R.layout.dialog_about);
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                if (AlipayZeroSdk.hasInstalledAlipayClient(MainActivity.this)) {
//                    builder.setNeutralButton(R.string.about_donate_alipay, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            AlipayZeroSdk.startAlipayClient(
//                                    MainActivity.this, "aehvyvf4taua18zo6e");
//                        }
//                    });
//                }
//                builder.show();
//            }
//
//        });

        // Set up cardView's top padding and system ui visibility
        LinearLayout cardView = findViewById(R.id.card_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mSettings.isDarkTheme()) {
            // PS: When Blackbulb runs on pre-23 API, it will be hard to see status bar icons
            //     because of light background. I don't want to fix it. User experience requires
            //     users to keep system version not out-of-date.
            cardView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        cardView.setPadding(0, Utility.getStatusBarHeight(this), 0, 0);

        // Set up toggle
        mToggle = findViewById(R.id.toggle);
        mSwitch = findViewById(R.id.toggle_switch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isRunning) {
                    if (!Utility.canDrawOverlays(MainActivity.this)) {
                        Utility.requestOverlayPermission(
                                MainActivity.this, REQUEST_CODE_OVERLAY_PERMISSION);
                        return;
                    }
                    startMaskService();
                } else {
                    stopMaskService();
                    NativeAdManager.getInstance().showAd("CLICK_MODE_SWITCH");
                }
            }
        });
//        mToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isRunning) {
//                    if (!Utility.canDrawOverlays(MainActivity.this)) {
//                        Utility.requestOverlayPermission(
//                                MainActivity.this, REQUEST_CODE_OVERLAY_PERMISSION);
//                        return;
//                    }
//                    startMaskService();
//                } else {
//                    stopMaskService();
//                    showNativeAd();
//                }
//            }
//        });

        // Set up seekBar
        mSeekBar = findViewById(R.id.seek_bar);
        setSeekBarProgress(mSettings.getBrightness(45) - 20);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentProgress = -1;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProgress = progress + 20;
                if (isRunning) {
                    // Only send broadcast when running
                    Intent intent = new Intent(MainActivity.this, MaskService.class);
                    intent.putExtra(Constants.Extra.ACTION, Constants.Action.UPDATE);
                    intent.putExtra(Constants.Extra.BRIGHTNESS, currentProgress);
                    startService(intent);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentProgress != -1) {
                    mSettings.setBrightness(currentProgress);
                }
            }
        });

        // Set up expandIcon
        mExpandIcon = findViewById(R.id.expand_icon);
        mExpandIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpand = !isExpand;
                updateExpandViews();
            }
        });

        mMiniSchedulerBar = findViewById(R.id.mini_scheduler_info);
        mMiniSchedulerStatus = findViewById(R.id.mini_scheduler_status_text);
        mDivider = findViewById(R.id.divider_line);

        // Show/Hide button tip
        mButtonTip = findViewById(R.id.button_tips);
        if (!mSettings.needButtonTip()) mButtonTip.setVisibility(View.GONE);

        // Init rows (Better not change initialization orders)
        initSchedulerRow();
        initDarkThemeRow();
        initYellowFilterRow();
        initAdvancedModeRow();

        updateExpandViews();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Utility.canDrawOverlays(MainActivity.this)) {
                    Utility.requestOverlayPermission(MainActivity.this, REQUEST_CODE_OVERLAY_PERMISSION);
                }
            }
        }, 1000);
    }

    private void updateExpandViews() {
        mExpandIcon.setState(isExpand ? ExpandIconView.LESS : ExpandIconView.MORE, true);
        int visibility = isExpand ? View.VISIBLE : View.GONE;
        mMiniSchedulerBar.setVisibility(
                !isExpand && mSettings.isAutoMode() ? View.VISIBLE : View.GONE);
        mDivider.setVisibility(visibility);
        mSchedulerRow.setVisibility(visibility);
        mDarkThemeRow.setVisibility(visibility);
        mYellowFilterRow.setVisibility(visibility);
        mAdvancedModeRow.setVisibility(visibility);
    }

    private void initSchedulerRow() {
        mSchedulerRow = findViewById(R.id.scheduler_row);
        mSchedulerIcon = findViewById(R.id.scheduler_icon);
        mSchedulerStatus = findViewById(R.id.tv_scheduler_status);
        Button settingsButton = findViewById(R.id.btn_scheduler_settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PowerManager pm = getSystemService(PowerManager.class);
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.dialog_ignore_battery_opt_title)
                                .setMessage(R.string.dialog_ignore_battery_opt_msg)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        showSchedulerDialog();
                                    }
                                })
                                .setNeutralButton(R.string.dialog_button_go_to_set,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Utility.requestBatteryOptimization(MainActivity.this);
                                            }
                                        })
                                .show();
                        return;
                    }
                }
                showSchedulerDialog();
            }
        });

        updateSchedulerRow();
    }

    private void initYellowFilterRow() {
        mYellowFilterRow = findViewById(R.id.yellow_filter_row);
        mYellowFilterSeekBar = findViewById(R.id.yellow_filter_seek_bar);

        mYellowFilterSeekBar.setProgress(mSettings.getYellowFilterAlpha());

        mYellowFilterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int currentProgress = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                currentProgress = progress;
                if (isRunning) {
                    // Only send broadcast when running
                    Intent intent = new Intent(MainActivity.this, MaskService.class);
                    intent.putExtra(Constants.Extra.ACTION, Constants.Action.UPDATE);
                    intent.putExtra(Constants.Extra.YELLOW_FILTER_ALPHA, currentProgress);
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentProgress != -1) {
                    mSettings.setYellowFilterAlpha(currentProgress);
                }
            }
        });
    }

    private void updateSchedulerRow() {
        mSchedulerIcon.setImageResource(mSettings.isAutoMode() ?
                R.drawable.ic_alarm_black_24dp : R.drawable.ic_alarm_off_black_24dp);
        if (mSettings.isAutoMode()) {
            String text;
            if (isRunning && AlarmUtil.isInSleepTime(this)) {
                text = getString(R.string.scheduler_status_on_show_disable_time,
                        mSettings.getSunriseTimeText());
            } else {
                text = getString(R.string.scheduler_status_on_show_enable_time,
                        mSettings.getSunsetTimeText());
            }
            mSchedulerStatus.setText(text);
            mMiniSchedulerStatus.setText(text);
        } else {
            mSchedulerStatus.setText(R.string.scheduler_status_off);
        }
    }

    private void initDarkThemeRow() {
        mDarkThemeRow = findViewById(R.id.dark_theme_row);
        Switch mDarkThemeSwitch = findViewById(R.id.dark_theme_switch);

        mDarkThemeSwitch.setChecked(mSettings.isDarkTheme());

        mDarkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSettings.setDarkTheme(isChecked);
                // Restart main activity after theme changed
                Intent intent = Intent.makeRestartActivityTask(getComponentName());
                startActivity(intent);
                moveTaskToBack(true);
            }
        });
    }

    private void initAdvancedModeRow() {
        mAdvancedModeRow = findViewById(R.id.advanced_mode_row);
        mAdvancedModeText = findViewById(R.id.advanced_mode_text);
        ImageButton settingsButton = findViewById(R.id.btn_advanced_mode_settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdvancedModeDialog();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settingsButton.setImageResource(R.drawable.ic_help_outline_black_24dp);
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show explanation
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.mode_android_oreo_explanation_dialog_title)
                            .setMessage(R.string.mode_android_oreo_explanation_dialog_message)
                            .setNeutralButton(R.string.mode_android_oreo_explanation_read_more,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mode_android_oreo_explanation_url))));
                                        }
                                    }
                            )
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });
        }

        updateAdvancedModeRow();
    }

    private void updateAdvancedModeRow() {
        // Color filter is not supported in overlay all mode.
        mYellowFilterSeekBar.setEnabled(
                mSettings.getAdvancedMode() != Constants.AdvancedMode.OVERLAY_ALL);
        mYellowFilterSeekBar.setProgress(
                mSettings.getAdvancedMode() != Constants.AdvancedMode.OVERLAY_ALL ?
                        mSettings.getYellowFilterAlpha() : 0);

        int textResId;
        switch (mSettings.getAdvancedMode()) {
            case Constants.AdvancedMode.NONE:
                textResId = R.string.mode_text_normal;
                break;
            case Constants.AdvancedMode.NO_PERMISSION:
                textResId = R.string.mode_text_no_permission;
                break;
            case Constants.AdvancedMode.OVERLAY_ALL:
                textResId = R.string.mode_text_overlay_all;
                break;
            default:
                throw new IllegalStateException("Unsupported advanced mode.");
        }
        mAdvancedModeText.setText(textResId);
    }

    private void showSchedulerDialog() {
        new SchedulerDialog(this, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateSchedulerRow();
            }
        }).show();
    }

    private void showAdvancedModeDialog() {
        int current = mSettings.getAdvancedMode();
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.dialog_choose_mode)
                .setSingleChoiceItems(new ModeListAdapter(current), current, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog modeDialog = (AlertDialog) dialog;
                        ModeListAdapter adapter =
                                (ModeListAdapter) modeDialog.getListView().getAdapter();
                        // Set mode value
                        mSettings.setAdvancedMode(adapter.getItem(which).getModeId());
                        updateAdvancedModeRow();
                        // Restart service
                        mToggle.performClick();
                        mToggle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mToggle.performClick();
                            }
                        }, 800);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
        }
        registerReceiver(mReceiver, new IntentFilter(Constants.ACTION_TOGGLE));

        // Request current state
        bindService(new Intent(this, MaskService.class),
                mServiceConnection, MaskService.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Utility.canDrawOverlays(this)) {
                    startMaskService();
//                    NativeAdManager.getInstance().showAd("REQUEST_PERMISSION_SUCCESSFUL");
                } else {
                    Utility.requestOverlayPermission(
                            MainActivity.this, REQUEST_CODE_OVERLAY_PERMISSION);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Support control brightness by volume buttons
        int action = event.getAction();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    setSeekBarProgress(mSeekBar.getProgress() - 5);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    setSeekBarProgress(mSeekBar.getProgress() + 5);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setSeekBarProgress(int progress) {
        progress = Math.max(0, Math.min(80, progress));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mSeekBar.setProgress(progress, true);
        } else {
            mSeekBar.setProgress(progress);
        }
    }

    private void setToggleIconState(boolean isRunning) {
        if (mToggle != null && !isFinishing()) {
            updateSchedulerRow();
            mToggle.setImageResource(isRunning ?
                    R.drawable.ic_brightness_2_black_24dp : R.drawable.ic_brightness_7_black_24dp);
            mSwitch.setChecked(isRunning);
        }
    }

    private void startMaskService() {
        if (mSettings.needButtonTip()) {
            mSettings.setNeedButtonTip(false);
            mButtonTip.setVisibility(View.GONE);
        }

        // Send start action
        ActionReceiver.sendActionStart(this);
        setToggleIconState(isRunning = true);

        // For safe
        if (mSettings.isFirstRun()) {
            if (mFirstRunDialog != null && mFirstRunDialog.isShowing()) {
                return;
            }
            hasDismissFirstRunDialog = false;
            mFirstRunDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.dialog_first_run_title)
                    .setMessage(R.string.dialog_first_run_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hasDismissFirstRunDialog = true;
                            mSettings.setFirstRun(false);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (hasDismissFirstRunDialog) return;
                            hasDismissFirstRunDialog = true;
                            if (mSettings.isFirstRun()) {
                                Intent intent1 =
                                        new Intent(MainActivity.this, MaskService.class);
                                intent1.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
                                stopService(intent1);
                                setToggleIconState(isRunning = false);
                            }
                        }
                    })
                    .show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mFirstRunDialog.isShowing() && !hasDismissFirstRunDialog) {
                        mFirstRunDialog.dismiss();
                    }
                }
            }, 5000);
        }
    }

    private void stopMaskService() {
        Intent intent = new Intent(MainActivity.this, MaskService.class);
        intent.putExtra(Constants.Extra.ACTION, Constants.Action.STOP);
        startService(intent);
        setToggleIconState(isRunning = false);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mToggle == null) return;
            int eventId = intent.getIntExtra(Constants.Extra.EVENT_ID, -1);
            switch (eventId) {
                case Constants.Event.CANNOT_START:
                    // Receive a error from MaskService
                    isRunning = false;
                    setToggleIconState(false);
                    if (!isFinishing()) {
                        Toast.makeText(
                                context.getApplicationContext(),
                                R.string.mask_fail_to_start,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    break;
                case Constants.Event.DESTROY_SERVICE:
                    // MaskService is destroying
                    if (isRunning) {
                        setToggleIconState(false);
                        isRunning = false;
                    }
                    break;
                case Constants.Event.CHECK:
                    // Receive check event and update toggle icon state
                    isRunning = intent.getBooleanExtra(Constants.Extra.IS_SHOWING, false);
                    Log.i(TAG, "Checked " + isRunning);
                    setToggleIconState(isRunning);
                    break;
            }
        }

    }

    @Override
    public void finish() {
        moveTaskToBack(true);
    }

}
