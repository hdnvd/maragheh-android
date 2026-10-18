package com.sweetsoft.data.model;

import java.util.List;

public class Paged<T> {
    public List<T> data;
    public Meta meta;

    public static class Meta {
        public int currentPage;
        public int lastPage;
        public int total;
    }

    public boolean hasMore() {
        return meta != null && meta.currentPage < meta.lastPage;
    }
}
