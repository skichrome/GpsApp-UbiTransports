package com.skichrome.gpsapp.model.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;

@Dao
public interface LocationDao
{
    @Insert
    Completable insertNewLocation(RoomLocation location);

    @Query("SELECT * FROM location")
    LiveData<List<RoomLocation>> observeLocations();

    @Query("DELETE FROM location")
    Completable flushLocationTable();
}