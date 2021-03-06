package com.skichrome.gpsapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.base.EditLocationRepository;
import com.skichrome.gpsapp.model.base.ReadLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.observers.DisposableCompletableObserver;

public class AverageSpeedFragmentViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private String TAG = getClass().getSimpleName();
    private boolean needToComputeAverage = true;

    private EditLocationRepository editRepository;
    private ReadLocationRepository readRepository;

    private LiveData<List<RoomLocation>> locations;
    private MutableLiveData<Double> average = new MutableLiveData<>();
    private MutableLiveData<Boolean> isInMovement = new MutableLiveData<>(false);

    public AverageSpeedFragmentViewModel(ReadLocationRepository readRepository, EditLocationRepository editRepository)
    {
        this.readRepository = readRepository;
        this.editRepository = editRepository;
    }

    // --- Getters --- //

    public LiveData<Boolean> getIsInMovement()
    {
        return isInMovement;
    }

    public LiveData<Double> getAverage()
    {
        return average;
    }

    public LiveData<List<RoomLocation>> getLocations()
    {
        if (locations == null)
        {
            locations = Transformations.map(readRepository.observeLocations(), locations ->
            {
                if (locations.isEmpty())
                    return locations;

                if (needToComputeAverage)
                {
                    computeAverage(locations);
                    needToComputeAverage = false;
                }
                return searchIfMovement(locations);
            });
        }
        return locations;
    }

    // =================================
    //              Methods
    // =================================

    private void computeAverage(List<RoomLocation> locationList)
    {
        int count = locationList.size();
        double speedSum = 0.0;
        for (RoomLocation location : locationList)
            speedSum += location.getSpeed();
        average.setValue(speedSum / count);
    }

    public void resetMovementIndicator()
    {
        isInMovement.setValue(false);
    }

    @NotNull
    private List<RoomLocation> searchIfMovement(List<RoomLocation> locationList)
    {
        // Check list size, it must be sufficient
        if (locationList.size() > 5)
        {
            // Counter to test if the vehicle is in movement (it must be at least a number of location speed over zero to consider a movement
            int movementCount = 0;
            List<RoomLocation> lastLocations = locationList.subList(locationList.size() - 5, locationList.size());

            for (RoomLocation lastLocation : lastLocations)
            {
                if (Math.round(lastLocation.getSpeed()) >= 5)
                    movementCount++;
            }
            if (movementCount >= 5)
                resetLocation();
        }
        return locationList;
    }

    private void resetLocation()
    {
        editRepository.deleteAllLocations()
                .subscribe(new DisposableCompletableObserver()
                {
                    @Override
                    public void onComplete()
                    {
                        isInMovement.setValue(true);
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.d(TAG, "Insert in database complete");
                    }
                });
        needToComputeAverage = true;
    }
}
