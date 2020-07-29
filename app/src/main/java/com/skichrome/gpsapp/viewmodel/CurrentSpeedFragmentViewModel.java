package com.skichrome.gpsapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
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

    private MutableLiveData<Boolean> isStopped;

    public CurrentSpeedFragmentViewModel(ReadLocationRepository repository)
    {
        this.repository = repository;
    }

    // --- Getters --- //

    public LiveData<List<RoomLocation>> getLocation()
    {
        if (location == null)
        {
            location = repository.observeLocations();
            location = Transformations.map(repository.observeLocations(), locationList ->
            {
                // Check list size, it must be sufficient
                if (locationList.size() > 5)
                {
                    // Counter to test if the vehicle is stationary (it must be at least a number of location speed near zero to consider a stop
                    int stopCount = 0;
                    List<RoomLocation> lastLocations = locationList.subList(locationList.size() - 5, locationList.size());
                    for (RoomLocation lastLocation : lastLocations)
                    {
                        if (Math.round(lastLocation.getSpeed()) <= 5)
                            stopCount++;
                        Log.e("CurrentSpeed", "stopCount: " + stopCount);
                    }
                    if (stopCount >= 5)
                        isStopped.setValue(true);
                }
                return locationList;
            });
        }
        return location;
    }

    public LiveData<Boolean> getStopped()
    {
        if (isStopped == null)
            isStopped = new MutableLiveData<>(false);
        return isStopped;
    }

    // =================================
    //              Methods
    // =================================

    public void sayHello()
    {
        Log.e(getClass().getSimpleName(), "sayHello: Hello world !");
    }

    public void resetStoppedIndicator()
    {
        isStopped.setValue(false);
    }
}
