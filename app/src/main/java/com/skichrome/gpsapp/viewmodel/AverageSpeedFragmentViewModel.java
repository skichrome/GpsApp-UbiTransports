package com.skichrome.gpsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.base.ReadLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;

public class AverageSpeedFragmentViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private ReadLocationRepository repository;

    private LiveData<List<RoomLocation>> location;

    public AverageSpeedFragmentViewModel(ReadLocationRepository repository)
    {
        this.repository = repository;
    }

    // --- Getters --- //

    public LiveData<List<RoomLocation>> getLocation()
    {
        if (location == null)
            location = repository.observeLocations();
        return location;
    }
}
