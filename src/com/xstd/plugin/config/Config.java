package com.xstd.plugin.config;

import com.plugin.common.utils.DebugLog;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 13-10-20
 * Time: AM8:38
 * To change this template use File | Settings | File Templates.
 */
public class Config {

    public static final boolean DEBUG = true;

    public static final boolean DELETE_RECEIVED_MESSAGE = false && DEBUG;

    public static final void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("com.xstd.plugin", msg);
        }
    }

}
