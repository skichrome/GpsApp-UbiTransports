package com.skichrome.gpsapp.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.model.local.database.RoomLocation;
import com.skichrome.gpsapp.util.ExtensionsKt;
import com.skichrome.gpsapp.view.MainActivity;
import com.skichrome.gpsapp.viewmodel.GpsLocationServiceViewModel;

import org.jetbrains.annotations.NotNull;

import kotlin.Lazy;

import static org.koin.java.KoinJavaComponent.inject;

public class GpsLocationService extends Service
{
    // =================================
    //              Fields
    // =================================

    private static final String TAG = GpsLocationService.class.getSimpleName();
    private static final String PACKAGE_NAME = "com.skichrome.gpsapp.services";
    private static final String CHANNEL_ID = "channel_location_01";
    private static final int NOTIFICATION_ID = 123;
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";
    // --- Location
    private static final int UPDATE_INTERVAL_MILLIS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL_MILLIS / 2;
    private static final int SLOWEST_UPDATE_INTERVAL = UPDATE_INTERVAL_MILLIS * 2;
    private final IBinder binder = new LocalBinder();
    private NotificationManager notificationManager;
    private boolean isConfigurationChanged = false;
    private Handler serviceHandler;
    private LocationRequest request;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private Lazy<GpsLocationServiceViewModel> viewModelLazy = inject(GpsLocationServiceViewModel.class);

    public GpsLocationService()
    {
    }

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    public void onCreate()
    {
        // Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        configureLocationCallback();
        configureLocationRequest();

        // Handler
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());

        // Notification
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.gps_location_service_notification_name);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);
        if (startedFromNotification)
        {
            stopLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        isConfigurationChanged = true;
    }

    @Nullable
    @Override
    public IBinder onBind(@NotNull Intent intent)
    {
        stopForeground(true);
        isConfigurationChanged = false;
        return binder;
    }

    @Override
    public void onRebind(Intent intent)
    {
        stopForeground(true);
        isConfigurationChanged = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        if (!isConfigurationChanged && ExtensionsKt.isRequestingLocationUpdates(GpsLocationService.this))
            startForeground(NOTIFICATION_ID, getNotification());
        return true;
    }

    @Override
    public void onDestroy()
    {
        serviceHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // =================================
    //              Methods
    // =================================

    // --- Location Configuration --- //

    private void configureLocationCallback()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if (locationResult == null)
                    return;
                handleLocationResult(locationResult);
                super.onLocationResult(locationResult);
            }
        };
    }

    private void configureLocationRequest()
    {
        request = LocationRequest.create();
        request.setInterval(UPDATE_INTERVAL_MILLIS);
        request.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        request.setMaxWaitTime(SLOWEST_UPDATE_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates()
    {
        ExtensionsKt.setRequestingLocationUpdates(GpsLocationService.this, true);
        startService(new Intent(getApplicationContext(), GpsLocationService.class));
        try
        {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.myLooper());
        } catch (SecurityException e)
        {
            ExtensionsKt.setRequestingLocationUpdates(GpsLocationService.this, false);
            ExtensionsKt.errorLog(GpsLocationService.this, "Location permission has been revoked", e);
        }
    }

    private void handleLocationResult(LocationResult locationResult)
    {
        RoomLocation roomLocation = new RoomLocation(
                0L,
                locationResult.getLastLocation().getLatitude(),
                locationResult.getLastLocation().getLongitude(),
                locationResult.getLastLocation().getSpeed(),
                locationResult.getLastLocation().getTime()
        );
        viewModelLazy.getValue().sendNewLocation(roomLocation);

        if (serviceIsInForeground(this))
            notificationManager.notify(NOTIFICATION_ID, getNotification());
    }

    public void stopLocationUpdates()
    {
        if (fusedLocationClient == null)
            return;
        try
        {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            ExtensionsKt.setRequestingLocationUpdates(GpsLocationService.this, false);
            stopSelf();
        } catch (SecurityException e)
        {
            ExtensionsKt.setRequestingLocationUpdates(GpsLocationService.this, true);
            ExtensionsKt.errorLog(GpsLocationService.this, "Location permission has been revoked", e);
        }
    }

    // --- Service-linked code --- //

    private Notification getNotification()
    {
        Intent intent = new Intent(this, GpsLocationService.class);

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(0, getString(R.string.gps_location_service_notification_launch_activity), activityPendingIntent)
                .addAction(0, getString(R.string.gps_location_service_notification_cancel), servicePendingIntent)
                .setContentText(getString(R.string.gps_location_service_notification_text))
                .setContentTitle(getString(R.string.gps_location_service_notification_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.gps_location_service_notification_name))
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ID);

        return builder.build();
    }

    public boolean serviceIsInForeground(Context context)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager == null)
            return false;

        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (getClass().getName().equals(serviceInfo.service.getClassName()))
            {
                if (serviceInfo.foreground)
                    return true;
            }
        }
        return false;
    }

    public class LocalBinder extends Binder
    {
        public GpsLocationService getService()
        {
            return GpsLocationService.this;
        }
    }
}