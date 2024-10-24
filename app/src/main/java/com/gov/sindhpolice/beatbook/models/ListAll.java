package com.gov.sindhpolice.beatbook.models;

public class ListAll {
    private int id;
    private String category;
    private String title;
    private String createdBy;
    private String createdAt;

    public ListAll(int id, String category, String title, String createdBy, String createdAt) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
