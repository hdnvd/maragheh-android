package com.sweetsoft.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Persian text helpers. */
public final class Fa {
    private static final char[] DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    private Fa() {}

    public static String digits(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? DIGITS[c - '0'] : c);
        }
        return sb.toString();
    }

    /** Converts Persian/Arabic digits to ASCII (for phone/code inputs). */
    public static String toLatinDigits(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= '۰' && c <= '۹') sb.append((char) ('0' + (c - '۰')));
            else if (c >= '٠' && c <= '٩') sb.append((char) ('0' + (c - '٠')));
            else sb.append(c);
        }
        return sb.toString();
    }

    public static String temp(double t) {
        return digits(Math.round(t));
    }

    /** "۵ دقیقه پیش" for the last week, otherwise the server-formatted Jalali date. */
    public static String timeAgo(String iso, String fallback) {
        String fb = fallback == null ? "" : fallback;
        if (iso == null) return fb;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = f.parse(iso);
            if (d == null) return fb;
            long sec = (System.currentTimeMillis() - d.getTime()) / 1000;
            if (sec < 0) return fb;
            if (sec < 60) return "لحظاتی پیش";
            if (sec < 3600) return digits(sec / 60) + " دقیقه پیش";
            if (sec < 86400) return digits(sec / 3600) + " ساعت پیش";
            if (sec < 7 * 86400) return digits(sec / 86400) + " روز پیش";
        } catch (ParseException ignored) {
        }
        return fb;
    }
}
