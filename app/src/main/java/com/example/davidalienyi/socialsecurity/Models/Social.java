package com.example.davidalienyi.socialsecurity.Models;

public class Social {
    private String id;
    private String socialHandle;
    private String dateCreated;
    private String password;
    private String title;
    private Integer days;
    private String lastUpdated;

    public Social (String id, String socialHandle, String dateCreated, String password, String title, Integer days, String lastUpdated) {
        this.id = id;
        this.socialHandle = socialHandle;
        this.dateCreated = dateCreated;
        this.password = password;
        this.title = title;
        this.days = days;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getSocialHandle() {
        return socialHandle;
    }

    public void setSocialHandle(String socialHandle) {
        this.socialHandle = socialHandle;
    }
    public String getTitle() {
        return title;
    }
    public Integer getDays() {
        return days;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
}
