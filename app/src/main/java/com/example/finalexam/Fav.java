package com.example.finalexam;

public class Fav {
    String id , description, createdAt, username, profimageurl, thumburl, uid;

    public Fav() {
    }

    public Fav(String id, String description, String createdAt, String username, String profimageurl, String thumburl, String uid) {
        this.id = id;
        this.description = description;
        this.createdAt = createdAt;
        this.username = username;
        this.profimageurl = profimageurl;
        this.thumburl = thumburl;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfimageurl() {
        return profimageurl;
    }

    public void setProfimageurl(String profimageurl) {
        this.profimageurl = profimageurl;
    }

    public String getThumburl() {
        return thumburl;
    }

    public void setThumburl(String thumburl) {
        this.thumburl = thumburl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "Fav{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", username='" + username + '\'' +
                ", profimageurl='" + profimageurl + '\'' +
                ", thumburl='" + thumburl + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
