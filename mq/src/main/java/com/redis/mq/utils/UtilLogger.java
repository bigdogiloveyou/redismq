//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.redis.mq.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;

public class UtilLogger {
    private static final char THREAD_RIGHT_TAG = ']';
    private static final char THREAD_LEFT_TAG = '[';
    private static final String KEY_VALUE_CONNECTOR = "=";
    private static final String KEY_VALUE_SEPARATOR = ";";

    private UtilLogger() {
    }

    public static void debug(Logger logger, Object... obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogString(obj));
        }

    }

    public static void info(Logger logger, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogString(obj));
        }

    }

    public static void infoDetail(Logger logger, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(getDetailLogString(obj));
        }

    }

    public static void warn(Logger logger, Object... obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogString(obj));
        }

    }

    public static void error(Logger logger, Throwable t, Object... obj) {
        if (logger.isErrorEnabled()) {
            logger.error(getLogString(obj), t);
        }

    }

    public static void warn(Logger logger, Throwable t, Object... obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLogString(obj), t);
        }

    }

    public static String getLogString(Object... obj) {
        StringBuilder log = new StringBuilder();
        log.append('[').append(Thread.currentThread().getId()).append(']');
        Object[] var2 = obj;
        int var3 = obj.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Object o = var2[var4];
            log.append(o);
        }

        return log.toString();
    }

    public static String getDetailLogString(Object... obj) {
        StringBuilder log = new StringBuilder();
        log.append('[').append(Thread.currentThread().getId()).append(']');
        Object[] var2 = obj;
        int var3 = obj.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Object o = var2[var4];
            if (o instanceof Map) {
                log.append(objectMap2string((Map)o));
            } else if (o instanceof String) {
                log.append((String)o);
            } else if (o instanceof List) {
                log.append(objectList2String((List)o));
            } else {
                log.append(ToStringBuilder.reflectionToString(o));
            }

            log.append(";");
        }

        return log.toString();
    }

    private static String objectList2String(List<Object> list) {
        if (null == list) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator var2 = list.iterator();

            while(var2.hasNext()) {
                Object obj = var2.next();
                sb.append(ToStringBuilder.reflectionToString(obj));
                sb.append(";");
            }

            return sb.toString();
        }
    }

    private static String objectMap2string(Map<String, Object> map) {
        if (null == map) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator var2 = map.keySet().iterator();

            while(var2.hasNext()) {
                String key = (String)var2.next();
                sb.append(key);
                sb.append("=");
                sb.append(StringUtils.defaultIfBlank(String.valueOf(map.get(key)), ""));
                sb.append(";");
            }

            return sb.toString();
        }
    }

    public static void debugByFormat(Logger logger, String format, Object... obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, obj);
        }

    }

    public static void infoByFormat(Logger logger, String format, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(format, obj);
        }

    }

    public static void warnByFormat(Logger logger, String format, Object... obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, obj);
        }

    }

    public static void errorByFormat(Logger logger, String format, Object... obj) {
        if (logger.isErrorEnabled()) {
            logger.error(format, obj);
        }

    }
}
