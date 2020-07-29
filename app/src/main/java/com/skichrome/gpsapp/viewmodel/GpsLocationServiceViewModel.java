package com.skichrome.gpsapp.viewmodel;

import android.location.Location;

import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.LocationRepository;

import java.util.List;

public class GpsLocationServiceViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private LocationRepository repository;

    public GpsLocationServiceViewModel(LocationRepository repository)
    {
        this.repository = repository;
    }

    public void sendNewLocation(List<Location> location)
    {
        repository.onNewLocation(location);
    }
}
