package com.sweetsoft.data.model;

import java.io.Serializable;

public class Photo implements Serializable {
    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";

    public long id;
    public String title;
    public String description;
    public String image;
    public String thumb;
    public int width;
    public int height;
    public String sender;
    public String status;
    public String rejectReason;
    public String createdAtFa;
}
