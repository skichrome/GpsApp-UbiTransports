package com.skichrome.gpsapp.model.base;

import androidx.lifecycle.LiveData;

import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;

public interface ReadLocationRepository
{
    LiveData<List<RoomLocation>> observeLocations();
}