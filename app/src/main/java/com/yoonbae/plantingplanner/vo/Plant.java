package com.yoonbae.plantingplanner.vo;

public class Plant {
    private String name;
    private String kind;
    private String imageName;
    private String imageUrl;
    private String intro;
    private String adoptionDate;
    private String alarm;
    private String alarmDate;
    private String period;
    private String alarmTime;
    private int alarmId;
    private String uid;
    private String userId;
    private String key;

    public Plant(String name, String kind, String imageName, String imageUrl, String intro, String adoptionDate, String alarm, String alarmDate, String period, String alarmTime, int alarmId, String uid, String userId, String key) {
        this.name = name;
        this.kind = kind;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.intro = intro;
        this.adoptionDate = adoptionDate;
        this.alarm = alarm;
        this.alarmDate = alarmDate;
        this.period = period;
        this.alarmTime = alarmTime;
        this.alarmId = alarmId;
        this.uid = uid;
        this.userId = userId;
        this.key = key;
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

/*    public void setKind(String kind) {
        this.kind = kind;
    }*/

    public String getImageName() {
        return this.imageName;
    }

/*    public void setImageName(String imageName) {
        this.imageName = imageName;
    }*/

    public String getImageUrl() {
        return this.imageUrl;
    }

/*    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }*/

    public String getIntro() {
        return this.intro;
    }

/*    public void setIntro(String intro) {
        this.intro = intro;
    }*/

    public String getAdoptionDate() {
        return this.adoptionDate;
    }

/*    public void setAdoptionDate(String adoptionDate) {
        this.adoptionDate = adoptionDate;
    }*/

    public String getAlarm() {
        return this.alarm;
    }

/*    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }*/

    public String getAlarmDate() {
        return this.alarmDate;
    }

/*    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }*/

    public String getPeriod() { return this.period; }

    //public void setPeriod(String period) { this.period = period; }

    public String getAlarmTime() {
        return this.alarmTime;
    }

/*    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }*/

    public String getUid() {
        return this.uid;
    }

/*    public void setUid(String uid) {
        this.uid = uid;
    }*/

/*    public String getUserId() {
        return this.userId;
    }*/

/*    public void setUserId(String userId) {
        this.userId = userId;
    }*/

    public int getAlarmId() {
        return this.alarmId;
    }

/*    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }*/

    public String getKey() { return this.key; }

    public void setKey(String key) { this.key = key; }
}
