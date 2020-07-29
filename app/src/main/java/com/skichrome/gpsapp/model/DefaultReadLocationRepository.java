package com.skichrome.gpsapp.model;

import androidx.lifecycle.LiveData;

import com.skichrome.gpsapp.model.base.LocationSource;
import com.skichrome.gpsapp.model.base.ReadLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;


public class DefaultReadLocationRepository implements ReadLocationRepository
{
    // =================================
    //              Fields
    // =================================

    private LocationSource localSource;

    public DefaultReadLocationRepository(LocationSource localSource)
    {
        this.localSource = localSource;
    }

    // =================================
    //        Superclass Methods
    // =================================


    @Override
    public LiveData<List<RoomLocation>> observeLocations()
    {
        return localSource.observeLocations();
    }
}
