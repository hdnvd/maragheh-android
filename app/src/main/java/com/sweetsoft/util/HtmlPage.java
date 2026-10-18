package com.sweetsoft.util;

import android.content.Context;
import android.content.res.Configuration;
import android.webkit.WebSettings;
import android.webkit.WebView;

/** Renders server HTML in a WebView with RTL layout and the app font. */
public final class HtmlPage {
    private HtmlPage() {}

    public static void render(WebView webView, String bodyHtml) {
        Context ctx = webView.getContext();
        boolean dark = (ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        String fg = dark ? "#E6EEF0" : "#26343B";
        String link = dark ? "#74C5BC" : "#187C80";
        String html = "<!doctype html><html dir='rtl' lang='fa'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>"
                + "@font-face{font-family:Vazirmatn;src:url('fonts/Vazirmatn-Regular.ttf');}"
                + "@font-face{font-family:Vazirmatn;font-weight:bold;src:url('fonts/Vazirmatn-Bold.ttf');}"
                + "html,body{margin:0;padding:0;background:transparent;}"
                + "body{font-family:Vazirmatn,sans-serif;font-size:16.5px;line-height:2;color:" + fg
                + ";text-align:justify;word-wrap:break-word;}"
                + "p{margin:0 0 14px}img{max-width:100%;height:auto;border-radius:14px;margin:8px 0}"
                + "a{color:" + link + ";text-decoration:none}h2,h3,h4{line-height:1.6;margin:18px 0 8px}"
                + "figure{margin:0}figcaption{font-size:13px;opacity:.7;text-align:center}"
                + "table{max-width:100%;border-collapse:collapse}iframe{max-width:100%}"
                + "</style></head><body>" + (bodyHtml == null ? "" : bodyHtml) + "</body></html>";
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(false);
        webView.setBackgroundColor(0);
        webView.setVerticalScrollBarEnabled(false);
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
    }
}
