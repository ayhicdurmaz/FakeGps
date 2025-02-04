package com.albatrose.fakegps.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.SimpleTimeZone;

@Entity(tableName = "location", indices = {@Index(value = {"unique_title"}, unique = true)})
public class Location {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "unique_title")
    private String title;
    private double lat;
    private double lng;
    private String timestamp;

    // Constructor
    public Location(double lat, double lng, String title, String timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}

