package com.redis.mq.utils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class UtilCompare {

    private UtilCompare() {}

    public static boolean isEqual(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (o2 == null) {
                return false;
            } else {
                if (o1 instanceof Date) {
                    return isDateEqual((Date) o1, (Date) o2);
                } else {
                    return o1.equals(o2);
                }
            }
        }
    }

    public static boolean isStringEqualIgnoreCase(String o1, String o2) {
        if (o1 == null) {
            if (o2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (o2 == null) {
                return false;
            } else {
                return o1.equalsIgnoreCase(o2);
            }
        }
    }

    public static boolean isEmpty(String str) {
        return (str == null || "".equals(str.trim()));
    }

    public static boolean isEmpty(CharSequence str) {
        return (str == null || "".equals(str));
    }

    public static boolean isNotEmpty(String str) {
        return (str != null && (!"".equals(str.trim())));
    }

    public static boolean isJsonStrNotEmpty(String str) {
        return (str != null && (!"".equals(str.trim())) && (!"null".equals(str)));
    }

    public static boolean isBigThan0(Integer intVal) {
        return (intVal != null && intVal.intValue() > 0);
    }

    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(Collection list) {
        return list != null && list.size() > 0;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection list) {
        return list == null || list.size() == 0;
    }

    public static boolean isNotEmpty(int[] values) {
        return values != null && values.length > 0;
    }

    public static boolean isNotEmpty(Object[] values) {
        return values != null && values.length > 0;
    }

    public static boolean isWordSeparator(char c) {
        return !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '\''));
    }

    /**
     * The reason why I made this method:
     * When I create a Date(), and then store it into mysql db, and then get the date from db. The date gotten from db becomes different from the original one.
     * And the different is millisecond. So I think it's caused by that the way db store the date is different from java, maybe not store the millisecond.
     * So I create this method to only compare year, month, day, hour, minute and seconds.
     * @param o1
     * @param o2
     * @return
     */
    private static boolean isDateEqual(Date o1, Date o2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(o1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(o2);
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)
                && c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
                && c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE) && c1.get(Calendar.SECOND) == c2
            .get(Calendar.SECOND));
    }
}
