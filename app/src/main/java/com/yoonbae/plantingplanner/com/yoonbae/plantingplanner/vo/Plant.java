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

    public String getKind() {
        return this.kind;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getIntro() {
        return this.intro;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public String getPeriod() {
        return this.period;
    }

    public String getUid() {
        return this.uid;
    }

    public String getUserId() {
        return this.userId;
    }
}
