package com.skichrome.gpsapp.model;

import com.skichrome.gpsapp.model.base.EditLocationRepository;
import com.skichrome.gpsapp.model.base.LocationSource;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import io.reactivex.Completable;

public class DefaultEditLocationRepository implements EditLocationRepository
{
    // =================================
    //              Fields
    // =================================

    private LocationSource localSource;

    public DefaultEditLocationRepository(LocationSource localSource)
    {
        this.localSource = localSource;
    }

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    public Completable saveNewLocations(RoomLocation locations)
    {
        return localSource.insertLocation(locations);
    }

    @Override
    public Completable deleteAllLocations()
    {
        return localSource.deleteAllLocations();
    }
}