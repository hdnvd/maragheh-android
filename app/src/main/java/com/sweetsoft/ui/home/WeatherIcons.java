package com.sweetsoft.ui.home;

/** Maps the API's weather icon keys to emoji (rendered in colour on every Android version we support). */
public final class WeatherIcons {
    private WeatherIcons() {}

    public static String emoji(String icon, boolean isDay) {
        if (icon == null) return "🌡️";
        switch (icon) {
            case "clear":
                return isDay ? "☀️" : "🌙";
            case "partly":
                return isDay ? "⛅" : "☁️";
            case "cloudy":
                return "☁️";
            case "fog":
                return "🌫️";
            case "drizzle":
                return "🌦️";
            case "rain":
                return "🌧️";
            case "snow":
                return "❄️";
            case "storm":
                return "⛈️";
            default:
                return "🌡️";
        }
    }
}
