package com.albatrose.fakegps.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addLocation(Location location);

    @Delete
    void deleteLocation(Location location);

    @Query("UPDATE location SET unique_title = :newTitle WHERE unique_title = :currentTitle")
    void updateTitleByLatLng(String currentTitle, String newTitle);

    @Query("SELECT * FROM location")
    List<Location> getAllLocations();

}
