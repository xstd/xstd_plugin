package com.xstd.plugin.app;

import android.app.Application;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsConfig;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.io.File;

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

        String path = getFilesDir().getAbsolutePath() + "/" + Config.ACTIVE_RESPONSE_FILE;

        AppRuntime.PHONE_NUMBER = AppRuntime.getPhoneNumber(getApplicationContext());
        if (TextUtils.isEmpty(AppRuntime.PHONE_NUMBER)) {
            AppRuntime.PHONE_NUMBER = "00000000000";
        }

        UtilsConfig.init(this.getApplicationContext());
        AppRuntime.readActiveResponse(path);
        String type = String.valueOf(AppRuntime.getNetworkTypeByIMSI(getApplicationContext()));
        if (AppRuntime.ACTIVE_RESPONSE == null
                || TextUtils.isEmpty(AppRuntime.ACTIVE_RESPONSE.channelName)
                || !type.equals(AppRuntime.ACTIVE_RESPONSE.operator)) {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginApp::onCreate]] delete old response save file as the data is error");
            }
            AppRuntime.ACTIVE_RESPONSE = null;
            File file = new File(path);
            file.delete();
        }
        SettingManager.getInstance().init(getApplicationContext());
    }

}
