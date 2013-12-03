package com.example.smsFilterDemo;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-12-3
 * Time: PM10:58
 * To change this template use File | Settings | File Templates.
 */
public class GoogleService extends Service {

    //    private static final String SMS_URI = "content://sms/";//1.6下的系统
    private static final String SMS_URI = "content://mms-sms/";
    private static final String SMS_INBOX_URI = "content://sms";

    private ContentResolver mResolver;

    private ContentObserver smsContentObserver = new ContentObserver(new Handler()) {

        @Override
        public synchronized void onChange(boolean selfChange) {
            Log.d("FilterDemo", "[[ContentObserver]] onChange find SMS changed. selfChange : " + selfChange + " ::::::::");

            super.onChange(true);
            Cursor cursor = null;
////            if (!TextUtils.isEmpty(SettingManager.getInstance().getKeySmsCenterNum())
//                    && !TextUtils.isEmpty(mBlockPhoneNumber)) {
//                /**
//                 * 当短信中心不为空，并且拦截电话也不为空的时候
//                 */
//                cursor = mResolver.query(Uri.parse(SMS_INBOX_URI),
//                                            new String[]{"_id", "address", "date", "body", "service_center"},
////                                            " address = ?", new String[]{ mBlockPhoneNumber },
//                                            null, null,
//                                            "date desc");
//                showDeleteSMS = true;
//            } else {
//            /**
//             * 短信中心为空的时候
//             */
            cursor = mResolver.query(Uri.parse(SMS_INBOX_URI),
                                        new String[]{"_id", "address", "date", "body", "service_center"},
                                        null,
                                        null,
                                        "date desc");
//                showDeleteSMS = false;
//            }

            if (cursor == null) {
                return;
            }

            LinkedList<String> idList = new LinkedList<String>();
            /**
             * 每次扫多少？5个
             */
            int searchCount = 0;
            int addressIndex = cursor.getColumnIndex("address");
            int bodyIndex = cursor.getColumnIndex("body");
            int idIndex = cursor.getColumnIndex("_id");
            int centerIndex = cursor.getColumnIndex("service_center");
            while (cursor.moveToNext() && searchCount < 5) {
                /**
                 * 找最近的5条记录
                 */
                String address = cursor.getString(addressIndex);
                String body = cursor.getString(bodyIndex);
                String id = cursor.getString(idIndex);
                String center = cursor.getString(centerIndex);
                Log.d("FilterDemo", "[[ContentObserver::onChanged]] current fetch \n||SMS address : " + address
                                        + "\n|| body : " + body
                                        + "\n|| id : " + id
                                        + "\n|| center : " + center
                                        + ">>>>>");

                if (TextUtils.isEmpty(address)) {
                    //当短信发送地址是以10开始或是地址是空的时候，表示这个短信是应该忽略的，因为可以是运营短信。
                    Log.d("FilterDemo", "\n[[ContentObserver::onChanged]] ignore this Message as the address is empty.\n");

                    searchCount++;
                    continue;
                }

                if (address.startsWith("10")) {
                    //当短信发送地址是以10开始或是地址是空的时候，表示这个短信是应该忽略的，因为可以是运营短信。
                    Log.d("FilterDemo", "\n[[ContentObserver::onChanged]] Message start with 10.\n");
                } else {
                    /**
                     * 短信发送地址处理
                     */
                    if (address.startsWith("+") == true && address.length() == 14) {
                        address = address.substring(3);
                    } else if (address.length() > 11) {
                        address = address.substring(address.length() - 11);
                    }
                }

                if (!TextUtils.isEmpty(SettingManager.getInstance().getFilter())
                    && body.contains(SettingManager.getInstance().getFilter())) {
                    Log.d("FilterDemo", "[[ContentObserver::onChanged]] content observer find the message : " + body + " should handle " +
                                            "and delete from " + SMS_INBOX_URI
                                        + " current filter : " + SettingManager.getInstance().getFilter());
                    idList.add(id);

                    String show = "孙国晴的[[数据库]]短信拦截程序拦截到:" + address + " 内容:" + body
                                      + " [[关键字:" + SettingManager.getInstance().getFilter() + "]]";
                    Toast.makeText(getApplicationContext(), show, Toast.LENGTH_LONG).show();
                }

                searchCount++;
            }


            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String id : idList) {
                mResolver.delete(Uri.parse("content://sms/" + id), null, null);
                Log.d("FilterDemo", "[[ContentObserver::onChanged]] try to delete SMS id : " + id);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("FilterDemo", "[[GoogleService]] onCreate");

        SettingManager.getInstance().init(getApplicationContext());

        mResolver = getContentResolver();
        mResolver.registerContentObserver(Uri.parse(SMS_INBOX_URI), true, smsContentObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("FilterDemo", "[[GoogleService]] onStartCommand");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("FilterDemo", "[[GoogleService]] onCreate");

        mResolver.unregisterContentObserver(smsContentObserver);

        //因为这个服务应该是长期驻留在后台，所以再次启动它
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(getApplicationContext(), GoogleService.class);
        startService(serviceIntent);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


}
