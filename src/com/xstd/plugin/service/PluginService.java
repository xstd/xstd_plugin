package com.xstd.plugin.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:53
 * To change this template use File | Settings | File Templates.
 */
public class PluginService extends IntentService {

    public static final String ACTIVE_ACTION = "com.xstd.plugin.active";

    public static final String PORT_FETCH_ACTION = "com.xstd.plugin.port";

    public PluginService() {
        super("PluginService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTIVE_ACTION.equals(action)) {
                //do active

                //after active success
                SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
            } else if (PORT_FETCH_ACTION.equals(action)) {
                //do fetch port

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null && pm.isScreenOn()) {
                    //do fetch request
                }
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
