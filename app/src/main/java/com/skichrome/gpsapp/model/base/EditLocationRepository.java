package com.skichrome.gpsapp.model.base;

import com.skichrome.gpsapp.model.local.database.RoomLocation;

import io.reactivex.Completable;

public interface EditLocationRepository
{
    Completable saveNewLocations(RoomLocation locations);
}