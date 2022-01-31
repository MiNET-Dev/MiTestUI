package com.minet.mitestui;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class TechnicianModel {

    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("MaxSessions")
    @Expose
    private Integer maxSessions;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Surname")
    @Expose
    private String surname;
    @SerializedName("Cell")
    @Expose
    private String cell;
    @SerializedName("Image")
    @Expose
    private String image;
    @SerializedName("Email")
    @Expose
    private String email;
    @SerializedName("Identification")
    @Expose
    private String identification;
    @SerializedName("status")
    @Expose
    private Integer status;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(Integer maxSessions) {
        this.maxSessions = maxSessions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}