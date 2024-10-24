package com.gov.sindhpolice.beatbook.models;

public class DetailItem {
    private String title;
    private String category;
    private String latitude;
    private String longitude;
    private String address;
    private String contactNo;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private String description;

    // Constructor
    public DetailItem(String title, String category, String latitude, String longitude, String address,
                      String contactNo, String createdBy, String createdAt, String updatedAt, String description) {
        this.title = title;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.contactNo = contactNo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getContactNo() { return contactNo; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getDescription() { return description; }
}
