package com.xstd.plugin.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-21
 * Time: PM10:34
 * To change this template use File | Settings | File Templates.
 */
public class PackageInstallBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Config.LOGD("[[PackageInstallBRC::onReceive]] >>>>>>");

        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {
                SettingManager.getInstance().init(context);
                if (!SettingManager.getInstance().getKeyHasBindingDevices()) {
                    // start binding devices
                    Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(context, DeviceBindBRC.class));
                    i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "（自定义区域2）");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }
    }

}
