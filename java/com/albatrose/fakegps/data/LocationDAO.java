package com.albatrose.fakegps.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationDAO {
    @Insert
    void addLocation(Location location);

    @Delete
    void deleteLocation(Location location);

    @Query("UPDATE location SET title = :newTitle WHERE title = :currentTitle")
    void updateTitleByLatLng(String currentTitle, String newTitle);

    @Query("SELECT * FROM location")
    List<Location> getAllLocations();

}
