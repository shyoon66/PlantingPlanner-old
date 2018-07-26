package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo;

public class Plant {
    private String name;
    private String kind;
    private String imageUrl;
    private String intro;
    private String startDate;
    private String period;
    private String uid;
    private String userId;

    public Plant() {

    }

    public Plant(String name, String kind, String imageUrl, String intro, String startDate, String period, String uid, String userId) {
        this.name = name;
        this.kind = kind;
        this.imageUrl = imageUrl;
        this.intro = intro;
        this.startDate = startDate;
        this.period = period;
        this.uid = uid;
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIntro() {
        return this.intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
