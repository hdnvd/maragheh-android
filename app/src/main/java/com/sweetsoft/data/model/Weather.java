package com.sweetsoft.data.model;

import java.util.List;

public class Weather {
    public Current current;
    public List<Day> daily;
    public String sunrise;
    public String sunset;

    public static class Current {
        public double temp;
        public double feelsLike;
        public int humidity;
        public double wind;
        public int code;
        public String text;
        public String icon;
        public boolean isDay;
    }

    public static class Day {
        public String date;
        public String dayName;
        public String dateFa;
        public int code;
        public String text;
        public String icon;
        public double max;
        public double min;
        public Integer rainChance;
    }
}
