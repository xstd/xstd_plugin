package com.xstd.plugin.api;

import android.os.Bundle;
import android.text.TextUtils;
import com.plugin.common.utils.UtilsRuntime;
import com.plugin.internet.core.InternetStringUtils;
import com.plugin.internet.core.NetWorkException;
import com.plugin.internet.core.RequestBase;
import com.plugin.internet.core.RequestEntity;

import java.util.TreeMap;
import java.util.Vector;

public class PMRequestBase<T> extends RequestBase<T> {

    public static String BASE_API_URL = "http://112.213.107.223:8080/sp-0.1";

    private static final String KEY_METHOD = "method";
    private static final String KEY_HTTP_METHOD = "httpMethod";

    @Override
    public RequestEntity getRequestEntity() throws NetWorkException {
        RequestEntity entity = super.getRequestEntity();
        return entity;
    }

    @Override
    public Bundle getParams() throws NetWorkException {
        Bundle params = super.getParams();

        Class<?> c = this.getClass();
        String method = params.getString(KEY_METHOD);
        if (TextUtils.isEmpty(method)) {
            throw new RuntimeException("Method Name MUST NOT be NULL");
        }

        if (!method.startsWith("http://")) {    //method可填为 http://url/xxx?a=1&b=2 或  feed.gets
            method = BASE_API_URL + method.replace('.', '/');
        }

        String httpMethod = params.getString(KEY_HTTP_METHOD);
        params.remove(KEY_HTTP_METHOD);
        params.remove(KEY_METHOD);
        params.putString(KEY_METHOD, method);
        params.putString(KEY_HTTP_METHOD, httpMethod);

        return params;
    }

//    private String getSig(Bundle params, String appSecretKey, String userSecretKey) {
//        if (params == null) {
//            return null;
//        }
//
//        if (params.size() == 0) {
//            return "";
//        }
//
//
//        TreeMap<String, String> sortParams = new TreeMap<String, String>();
//        for (String key : params.keySet()) {
//            sortParams.put(key, params.getString(key));
//        }
//
//        Vector<String> vecSig = new Vector<String>();
//        for (String key : sortParams.keySet()) {
//            String value = sortParams.get(key);
//            vecSig.add(key + "=" + value);
//        }
//
//        String[] nameValuePairs = new String[vecSig.size()];
//        vecSig.toArray(nameValuePairs);
//
//        for (int i = 0; i < nameValuePairs.length; i++) {
//            for (int j = nameValuePairs.length - 1; j > i; j--) {
//                if (nameValuePairs[j].compareTo(nameValuePairs[j - 1]) < 0) {
//                    String temp = nameValuePairs[j];
//                    nameValuePairs[j] = nameValuePairs[j - 1];
//                    nameValuePairs[j - 1] = temp;
//                }
//            }
//        }
//        StringBuffer nameValueStringBuffer = new StringBuffer();
//        for (int i = 0; i < nameValuePairs.length; i++) {
//            nameValueStringBuffer.append(nameValuePairs[i]);
//        }
//        nameValueStringBuffer.append(appSecretKey);
//        if (!TextUtils.isEmpty(userSecretKey)) {
//            nameValueStringBuffer.append(userSecretKey);
//        }
//
////        if (AppConfig.DEBUG) {
////            for (int i = 0; i < nameValueStringBuffer.toString().length(); ) {
////                if (i + 1024 < nameValueStringBuffer.toString().length()) {
////                    Log.v("signa", nameValueStringBuffer.toString().substring(i, i + 1024));
////                } else {
////                    Log.v("signa", nameValueStringBuffer.toString().substring(i));
////                }
////                i = i + 1024;
////            }
////
////            AppConfig.LOGD("[[gtiSig]] sig raw : " + nameValueStringBuffer.toString());
////        }
//
//        String sig = InternetStringUtils.MD5Encode(nameValueStringBuffer.toString());
//        return sig;
//
//    }

}
