package com.whoamie.cinetime_nepal.member.models;

public class User {

    private String updated_at;
    private String created_at;
    private String reset_token;
    private String facebook_id;
    private String firebase_reg_id;
    private String bio;
    private double lon;
    private double lat;
    private String profile_pic_url;
    private String email_verified_at;
    private String phone;
    private String email;
    private String name;
    private int id;
    private int status;

    public int getStatus() {
        return status;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getReset_token() {
        return reset_token;
    }

    public String getFacebook_id() {
        return facebook_id;
    }

    public String getFirebase_reg_id() {
        return firebase_reg_id;
    }

    public String getBio() {
        return bio;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public String getEmail_verified_at() {
        return email_verified_at;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setReset_token(String reset_token) {
        this.reset_token = reset_token;
    }

    public void setFacebook_id(String facebook_id) {
        this.facebook_id = facebook_id;
    }

    public void setFirebase_reg_id(String firebase_reg_id) {
        this.firebase_reg_id = firebase_reg_id;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }

    public void setEmail_verified_at(String email_verified_at) {
        this.email_verified_at = email_verified_at;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }
}
