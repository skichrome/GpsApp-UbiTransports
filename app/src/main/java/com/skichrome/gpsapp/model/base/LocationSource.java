package com.skichrome.gpsapp.model.base;

import androidx.lifecycle.LiveData;

import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;

import io.reactivex.Completable;

public interface LocationSource
{
    Completable insertLocation(RoomLocation location);

    LiveData<List<RoomLocation>> observeLocations();

    Completable deleteAllLocations();
}