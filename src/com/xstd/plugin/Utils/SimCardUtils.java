package com.xstd.plugin.Utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by michael on 13-12-5.
 */
public class SimCardUtils {

    public static boolean issDoubleTelephone(Context context) {
        boolean isDouble = true;
        Method method = null;
        Object result_0 = null;
        Object result_1 = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            //只要在反射getSimStateGemini 这个函数时报了错就是单卡手机（这是我自己的经验，不一定全正确）
            method = TelephonyManager.class.getMethod("getSimStateGemini", new Class[]{int.class});
            //获取SIM卡1
            result_0 = method.invoke(tm, new Object[]{new Integer(0)});
            //获取SIM卡1
            result_1 = method.invoke(tm, new Object[]{new Integer(1)});
        } catch (SecurityException e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("1_ISSINGLETELEPHONE:"+e.toString());
        } catch (NoSuchMethodException e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("2_ISSINGLETELEPHONE:"+e.toString());
        } catch (IllegalArgumentException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (Exception e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("3_ISSINGLETELEPHONE:"+e.toString());
        }

        return isDouble;
    }

//    public static String getActivePhoneType(Context context) {
//        ITelephony iTelephony = getITelephony(context); //获取电话接口
//        if (iTelephony != null) {
//            try {
//                int type = iTelephony.getActivePhoneType();
//                switch (type) {
//                    case 1:
//                        return "GSM";
//                    case 2:
//                        return "CDMA";
//                    default:
//                        return "未知";
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return "获取iTelephony失败";
//    }

    private static ITelephony getITelephony(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod = null;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null); // 获取声明的方法
            getITelephonyMethod.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null); // 获取实例
            return iTelephony;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSimCardReadyInfo(Context context) {
        boolean isDouble = true;
        Method method = null;
        Object result_0 = null;
        Object result_1 = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            //只要在反射getSimStateGemini 这个函数时报了错就是单卡手机（这是我自己的经验，不一定全正确）
            method = TelephonyManager.class.getMethod("getSimStateGemini", new Class[]{int.class});
            //获取SIM卡1
            result_0 = method.invoke(tm, new Object[]{new Integer(0)});
            //获取SIM卡1
            result_1 = method.invoke(tm, new Object[]{new Integer(1)});
        } catch (SecurityException e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("1_ISSINGLETELEPHONE:"+e.toString());
        } catch (NoSuchMethodException e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("2_ISSINGLETELEPHONE:"+e.toString());
        } catch (IllegalArgumentException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            isDouble = false;
            e.printStackTrace();
        } catch (Exception e) {
            isDouble = false;
            e.printStackTrace();
            //System.out.println("3_ISSINGLETELEPHONE:"+e.toString());
        }

        if (isDouble) {
            if (result_0.toString().equals("5") && result_1.toString().equals("5")) {
                return "双卡手机，两张卡都可用";
            } else if (!result_0.toString().equals("5") && result_1.toString().equals("5")) {//卡二可用
                return "双卡手机，第二张卡可用";
            } else if (result_0.toString().equals("5") && !result_1.toString().equals("5")) {//卡一可用
                return "双卡手机，第一张卡可用";
            } else {//两个卡都不可用(飞行模式会出现这种种情况)
                return "双卡手机，都不可用";
            }
        }

        return "不是双卡手机，获取双卡信息错误";
    }

}
