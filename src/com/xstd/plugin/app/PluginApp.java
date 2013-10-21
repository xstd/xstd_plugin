package com.xstd.plugin.app;

import android.app.Application;
import com.plugin.common.utils.UtilsConfig;
import com.xstd.plugin.config.SettingManager;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:35
 * To change this template use File | Settings | File Templates.
 */
public class PluginApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        UtilsConfig.init(this.getApplicationContext());
        SettingManager.getInstance().init(getApplicationContext());
    }

}
