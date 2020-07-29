package com.skichrome.gpsapp.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.skichrome.gpsapp.model.base.EditLocationRepository;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

public class GpsLocationServiceViewModel extends ViewModel
{
    // =================================
    //              Fields
    // =================================

    private String TAG = GpsLocationServiceViewModel.class.getSimpleName();

    private EditLocationRepository repository;

    public GpsLocationServiceViewModel(EditLocationRepository repository)
    {
        this.repository = repository;
    }

    // =================================
    //              Methods
    // =================================

    public void sendNewLocation(RoomLocation location)
    {
        CompletableObserver observer = repository.saveNewLocations(location)
                .subscribeWith(new CompletableObserver()
                {
                    @Override
                    public void onSubscribe(Disposable d)
                    {
                        Log.d(TAG, "Observer is subscribing now");
                    }

                    @Override
                    public void onComplete()
                    {
                        Log.d(TAG, "Insert in database complete");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e(TAG, "An error occurred when insert new location", e);
                    }
                });
    }
}
