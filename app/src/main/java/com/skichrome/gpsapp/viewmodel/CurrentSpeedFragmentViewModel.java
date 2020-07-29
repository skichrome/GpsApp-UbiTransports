package com.skichrome.gpsapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.base.ReadLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CurrentSpeedFragmentViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private ReadLocationRepository repository;

    private LiveData<List<RoomLocation>> locations;

    private MutableLiveData<Boolean> isStopped = new MutableLiveData<>(false);

    public CurrentSpeedFragmentViewModel(ReadLocationRepository repository)
    {
        this.repository = repository;
    }

    // --- Getters --- //

    public LiveData<List<RoomLocation>> getLocations()
    {
        if (locations == null)
        {
            locations = repository.observeLocations();
            locations = Transformations.map(repository.observeLocations(), this::searchIfStopped);
        }
        return locations;
    }

    public LiveData<Boolean> getStopped()
    {
        return isStopped;
    }

    // =================================
    //              Methods
    // =================================

    public void resetStoppedIndicator()
    {
        isStopped.setValue(false);
    }

    @NotNull
    private List<RoomLocation> searchIfStopped(List<RoomLocation> locationList)
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
                Log.e("searchIfStopped", "stopCount: " + stopCount);
            }
            if (stopCount >= 3)
                isStopped.setValue(true);
        }
        return locationList;
    }
}
