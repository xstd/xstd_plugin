package com.xstd.plugin.binddevice;

import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisabled]] action : " + intent.getAction());
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(false);
    }

}
