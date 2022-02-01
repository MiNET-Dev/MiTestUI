package com.minet.mitestui;

public class QCCheckListInfo {

    private Boolean isChecked;
    private String name;

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

    public Boolean getIsChecked(){ return isChecked; }

    public String getQCName(){ return name; }

}
