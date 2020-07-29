package com.skichrome.gpsapp.model.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RoomLocation.class}, version = 1)
public abstract class GpsAppDatabase extends RoomDatabase
{
    public abstract LocationDao locationDao();
}