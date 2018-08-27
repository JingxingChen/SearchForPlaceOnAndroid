package com.edu.usc.example.placessearch;

import java.io.Serializable;

public class Place implements Serializable{
    private static final long serialVersionUID=1L;
    private String icon;
    private String placeName;
    private String locationName;
    private String placeId;
    private int id = -1;

    public  Place(){}

    public  Place(String icon,String placeName,String locationName, String placeId,int id){
        this.icon = icon;
        this.placeName=placeName;
        this.locationName=locationName;
        this.placeId=placeId;
        this.id = id;
    }
    public String getIcon(){
        return icon;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public int getId() {
        return id;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setId(int id) {
        this.id = id;
    }
}
