package com.skichrome.gpsapp.model;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class DefaultLocationRepository implements LocationRepository
{
    private MutableLiveData<List<Location>> locationLiveData;

    @Override
    public void onNewLocation(List<Location> locations)
    {
        locationLiveData = new MutableLiveData<>(locations);
    }

    @Override
    public MutableLiveData<List<Location>> getLocations()
    {
        if (locationLiveData == null)
            locationLiveData = new MutableLiveData<>();
        return new MutableLiveData<>(locationLiveData.getValue());
    }
}
