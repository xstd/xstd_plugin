package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-7
 * Time: PM12:10
 * To change this template use File | Settings | File Templates.
 */
public class ScreenOnOffBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Config.LOGD("[[ScreenOnOffBRC::onReceive]] action = " + intent.getAction());
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (AppRuntime.LOCK_DEVICE_AS_DISDEVICE) {
                    UtilsRuntime.goHome(context);
                    AppRuntime.LOCK_DEVICE_AS_DISDEVICE = false;
                    //TODO: 可以在此时启动一个看门狗service，来检查是否推倒home
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            }
        }
    }

}
