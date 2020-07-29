package com.skichrome.gpsapp.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.LocationRepository;

import java.util.Collections;
import java.util.List;

public class ActivityMainViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private LocationRepository repository;
    private MutableLiveData<List<Location>> location;

    public ActivityMainViewModel(LocationRepository repository)
    {
        this.repository = repository;
    }

    // --- Getters --- //

    public LiveData<List<Location>> getLocation()
    {
        if (location == null)
        {
            location = new MutableLiveData<>(Collections.emptyList());
            fetchLocation();
        }
        return location;
    }

    // =================================
    //              Methods
    // =================================

    public void sayHello()
    {
        Log.e("ActivityVM", "sayHello: Hello world !");
    }

    private void fetchLocation()
    {
        location = repository.getLocations();
    }
}
