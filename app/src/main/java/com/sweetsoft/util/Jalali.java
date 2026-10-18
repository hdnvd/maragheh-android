package com.sweetsoft.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/** Minimal Jalali (Solar Hijri) <-> Gregorian conversion. */
public final class Jalali {
    public static final String[] MONTHS = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };

    public final int year;
    public final int month; // 1..12
    public final int day;

    public Jalali(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static Jalali today() {
        Calendar c = Calendar.getInstance();
        return fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static Jalali fromGregorian(int gy, int gm, int gd) {
        int[] gDm = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int gy2 = (gm > 2) ? (gy + 1) : gy;
        long days = 355666 + (365L * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDm[gm - 1];
        long jy = -1595 + (33 * (days / 12053));
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm, jd;
        if (days < 186) {
            jm = 1 + (int) (days / 31);
            jd = 1 + (int) (days % 31);
        } else {
            jm = 7 + (int) ((days - 186) / 30);
            jd = 1 + (int) ((days - 186) % 30);
        }
        return new Jalali((int) jy, jm, jd);
    }

    /** @return {year, month(1..12), day} */
    public int[] toGregorian() {
        long jy = year + 1595;
        long days = -355668 + (365L * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + day
                + ((month < 7) ? (month - 1) * 31 : ((month - 7) * 30) + 186);
        long gy = 400 * (days / 146097);
        days %= 146097;
        if (days > 36524) {
            gy += 100 * (--days / 36524);
            days %= 36524;
            if (days >= 365) days++;
        }
        gy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            gy += ((days - 1) / 365);
            days = (days - 1) % 365;
        }
        long gd = days + 1;
        boolean leap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0);
        int[] salA = {0, 31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int gm;
        for (gm = 0; gm < 13 && gd > salA[gm]; gm++) gd -= salA[gm];
        return new int[]{(int) gy, gm, (int) gd};
    }

    public static int monthLength(int year, int month) {
        if (month <= 6) return 31;
        if (month <= 11) return 30;
        int[] a = new Jalali(year, 12, 1).toGregorian();
        int[] b = new Jalali(year + 1, 1, 1).toGregorian();
        long diff = new GregorianCalendar(b[0], b[1] - 1, b[2]).getTimeInMillis()
                - new GregorianCalendar(a[0], a[1] - 1, a[2]).getTimeInMillis();
        return (int) Math.round(diff / 86400000.0);
    }

    /** Gregorian date as yyyy-MM-dd, the format the API expects for filters. */
    public String toIsoGregorian() {
        int[] g = toGregorian();
        return String.format(Locale.US, "%04d-%02d-%02d", g[0], g[1], g[2]);
    }

    public Jalali minusDays(int n) {
        int[] g = toGregorian();
        Calendar c = new GregorianCalendar(g[0], g[1] - 1, g[2]);
        c.add(Calendar.DAY_OF_MONTH, -n);
        return fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public String format() {
        return Fa.digits(day + " " + MONTHS[month - 1] + " " + year);
    }

    public boolean isAfter(Jalali o) {
        if (year != o.year) return year > o.year;
        if (month != o.month) return month > o.month;
        return day > o.day;
    }
}
