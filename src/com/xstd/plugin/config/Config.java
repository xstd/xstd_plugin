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

    public static final boolean DEBUG_IF_GO_HOME = true && DEBUG;

    public static final String DEFAULT_BASE_URL = "http://www.xinsuotd.com";

    public static final String CHANNEL_CODE = "100001";

    public static final String ACTIVE_RESPONSE_FILE = "response.data";

    public static final void LOGD(String msg) {
        if (DEBUG) {
            DebugLog.d("com.xstd.plugin", msg);
        }
    }

    public static final void LOGD(String msg, Throwable e) {
        if (DEBUG) {
            DebugLog.d("com.xstd.plugin", msg, e);
        }
    }

}
