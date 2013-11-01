package com.xstd.plugin.apn;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.xstd.plugin.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-30
 * Time: PM3:10
 * To change this template use File | Settings | File Templates.
 */
public class APNHelper {

    public static final int YI_DONG = 1000;
    public static final int LIAN_TONG = 1001;
    public static final int DIAN_XIN = 1002;

    public static class APNNet {
        /**
         * 中国移动cmwap
         */
        public static String CMWAP = "cmwap";
        /**
         * 中国移动cmnet
         */
        public static String CMNET = "cmnet";
        //中国联通3GWAP设置 中国联通3G因特网设置 中国联通WAP设置 中国联通因特网设置
        //3gwap 3gnet uniwap uninet
        /**
         * 3G wap 中国联通3gwap APN
         */
        public static String GWAP_3 = "3gwap";
        /**
         * 3G net 中国联通3gnet APN
         */
        public static String GNET_3 = "3gnet";
        /**
         * uni wap 中国联通uni wap APN
         */
        public static String UNIWAP = "uniwap";
        /**
         * uni net 中国联通uni net APN
         */
        public static String UNINET = "uninet";
    }

    public static String matchAPN(String currentName) {
        if ("".equals(currentName) || null == currentName) {
            return "";
        }
        currentName = currentName.toLowerCase();
        if (currentName.startsWith(APNNet.CMNET))
            return APNNet.CMNET;
        else if (currentName.startsWith(APNNet.CMWAP))
            return APNNet.CMWAP;
        else if (currentName.startsWith(APNNet.GNET_3))
            return APNNet.GNET_3;
        else if (currentName.startsWith(APNNet.GWAP_3))
            return APNNet.GWAP_3;
        else if (currentName.startsWith(APNNet.UNINET))
            return APNNet.UNINET;
        else if (currentName.startsWith(APNNet.UNIWAP))
            return APNNet.UNIWAP;
        else if (currentName.startsWith("default"))
            return "default";
        else return "";
    }

    public static int matchAPNForNetwork(String currentName) {
        if ("".equals(currentName) || null == currentName) {
            return -1;
        }
        currentName = currentName.toLowerCase();
        if (currentName.startsWith(APNNet.CMNET))
            return YI_DONG;
        else if (currentName.startsWith(APNNet.CMWAP))
            return YI_DONG;
        else if (currentName.startsWith(APNNet.GNET_3))
            return LIAN_TONG;
        else if (currentName.startsWith(APNNet.GWAP_3))
            return LIAN_TONG;
        else if (currentName.startsWith(APNNet.UNINET))
            return LIAN_TONG;
        else if (currentName.startsWith(APNNet.UNIWAP))
            return LIAN_TONG;
        else if (currentName.startsWith("default"))
            return -1;
        else return -1;
    }

    public static class APNInfo {
        public String id;
        public String apn;
        public String type;
        public String current;

        @Override
        public String toString() {
            return "APNInfo{" +
                       "id='" + id + '\'' +
                       ", apn='" + apn + '\'' +
                       ", type='" + type + '\'' +
                       ", current='" + current + '\'' +
                       '}';
        }
    }

    /**
     * 通过APN类型来判断当前的运营商是什么
     */
    public static int getNetworkTypeByAPN(Context context) {
        List<APNInfo> list = getAPNList(context);
        if (list.size() > 0) {
            APNInfo info = list.get(0);
            String check = info.apn;
            if (TextUtils.isEmpty(check)) {
                check = info.type;
            }

            return matchAPNForNetwork(check);
        }

        return -1;
    }

    public static final Uri APN_URI = Uri.parse("content://telephony/carriers");
    public static final Uri CUR_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    public static List<APNInfo> getAPNList(Context context) {
        //current不为空表示可以使用的APN
        String projection[] = {"_id, apn, type, current"};
        Cursor cr = context.getContentResolver().query(CUR_APN_URI, projection, null, null, null);
        List<APNInfo> list = new ArrayList<APNInfo>();
        while (cr != null && cr.moveToNext()) {
            APNInfo a = new APNInfo();
            a.id = cr.getString(cr.getColumnIndex("_id"));
            a.apn = cr.getString(cr.getColumnIndex("apn"));
            a.type = cr.getString(cr.getColumnIndex("type"));
            a.current = cr.getString(cr.getColumnIndex("current"));
            list.add(a);
        }
        if (cr != null) {
            cr.close();
        }

        Config.LOGD("[[getAPNList]] list : " + list);

        return list;
    }
}
