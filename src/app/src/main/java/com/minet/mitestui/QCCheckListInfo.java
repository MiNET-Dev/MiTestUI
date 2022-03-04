package com.minet.mitestui;

public class QCCheckListInfo {

    private Boolean isChecked;
    private String name;
    private long timestamp;

    public QCCheckListInfo(Boolean isChecked, String name, long timestamp){
        this.isChecked = isChecked;
        this.name = name;
        this.timestamp = timestamp;
    }

    public QCCheckListInfo(Boolean isChecked, String name){
        this.isChecked = isChecked;
        this.name = name;
    }

    public void setIsChecked(Boolean isChecked){
        this.isChecked = isChecked;
    }

    public void setQCName(String name){
        this.name = name;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public Boolean getIsChecked(){ return isChecked; }

    public String getQCName(){ return name; }

    public long getTimestamp(){ return timestamp; }

}
