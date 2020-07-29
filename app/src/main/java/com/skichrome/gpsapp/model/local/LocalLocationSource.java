package com.skichrome.gpsapp.model.local;

import androidx.lifecycle.LiveData;

import com.skichrome.gpsapp.model.base.LocationSource;
import com.skichrome.gpsapp.model.local.database.LocationDao;
import com.skichrome.gpsapp.model.local.database.RoomLocation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LocalLocationSource implements LocationSource
{
    // =================================
    //              Fields
    // =================================

    private LocationDao locationDao;

    public LocalLocationSource(LocationDao locationDao)
    {
        this.locationDao = locationDao;
    }

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    public Completable insertLocation(RoomLocation location)
    {
        return locationDao.insertNewLocation(location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);
    }

    @Override
    public LiveData<List<RoomLocation>> observeLocations()
    {
        return locationDao.observeLocations();
    }

    @Override
    public Completable deleteAllLocations()
    {
        return locationDao.flushLocationTable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS);
    }
}