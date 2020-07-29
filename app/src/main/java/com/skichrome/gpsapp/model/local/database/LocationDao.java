package com.skichrome.gpsapp.model.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

@Dao
public interface LocationDao
{
    @Insert
    Completable insertNewLocation(RoomLocation location);

    @Query("SELECT * FROM location")
    Observable<List<RoomLocation>> fetchAllLocations();

    @Query("SELECT * FROM location")
    LiveData<List<RoomLocation>> observeLocations();
}