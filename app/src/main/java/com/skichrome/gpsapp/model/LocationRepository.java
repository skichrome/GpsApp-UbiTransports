package com.skichrome.gpsapp.model;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public interface LocationRepository
{
    void onNewLocation(List<Location> locations);

    MutableLiveData<List<Location>> getLocations();
}