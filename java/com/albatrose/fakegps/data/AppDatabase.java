package com.albatrose.fakegps.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Location.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase{
    public abstract LocationDAO locationDAO();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database").fallbackToDestructiveMigration().build();

                }
            }
        }
        return INSTANCE;
    }
}
