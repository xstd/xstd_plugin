package com.example.smsFilterDemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-12-3
 * Time: PM11:09
 * To change this template use File | Settings | File Templates.
 */
public class BootCompleted extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        SettingManager.getInstance().setServiceStart(true);

        Intent i = new Intent();
        i.setClass(context, FilterService.class);
        context.startService(i);
    }

}
