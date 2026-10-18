package com.sweetsoft.data.model;

import java.util.List;

public class Post {
    public static final String TYPE_ATTRACTION = "attraction";
    public static final String TYPE_CELEBRITY = "celebrity";

    public long id;
    public String type;
    public String title;
    public String subtitle;
    public String summary;
    public String bodyHtml;
    public String cover;
    public List<String> images;
    public Double latitude;
    public Double longitude;
}
