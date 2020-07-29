package com.skichrome.gpsapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.base.ReadLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;

public class CurrentSpeedFragmentViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private ReadLocationRepository repository;

    private LiveData<List<RoomLocation>> location;

    public CurrentSpeedFragmentViewModel(ReadLocationRepository repository)
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

    // =================================
    //              Methods
    // =================================

    public void sayHello()
    {
        Log.e(getClass().getSimpleName(), "sayHello: Hello world !");
    }
}
