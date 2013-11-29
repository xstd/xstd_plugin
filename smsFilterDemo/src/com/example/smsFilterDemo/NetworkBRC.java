package com.example.smsFilterDemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-29
 * Time: PM1:47
 * To change this template use File | Settings | File Templates.
 */
public class NetworkBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        SettingManager.getInstance().init(context);
        if (SettingManager.getInstance().getServiceStart()) {
            Intent i = new Intent();
            i.setClass(context, FilterService.class);
            context.startService(i);
        }
    }

}
