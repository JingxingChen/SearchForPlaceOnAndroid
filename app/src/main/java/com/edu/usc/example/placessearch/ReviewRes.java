package com.edu.usc.example.placessearch;



public class ReviewRes{
    private String icon;
    private String name;
    private String time;
    private String text;
    private String url;
    private int rate;
    private int id;
    public ReviewRes(){}

    public ReviewRes(String icon,String name,String time,String text,int rate){
        this.icon = icon;
        this.name = name;
        this.time = time;
        this.text = text;
        this.rate = rate;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getRate() {
        return rate;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
