package com.skichrome.gpsapp.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.ActivityMainBinding;
import com.skichrome.gpsapp.util.ExtensionsKt;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    // =================================
    //              Fields
    // =================================

    private ActivityMainBinding binding;

    private static final int UPDATE_INTERVAL_MILLIS = 1000;
    private static final int FAsTEST_UPDATE_INTERVAL = UPDATE_INTERVAL_MILLIS / 2;
    private static final int REQUEST_CHECK_SETTINGS = 1234;
    private static final String LOCATION_UPDATES_STATE_KEY = "location_updates_request_are_enabled";

    private boolean canLaunchRequestLocation;
    private LocationRequest request;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        restoreValuesInBundle(savedInstanceState);
        configureUI();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        MainActivityPermissionsDispatcher.beginLocationUpdatesWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (canLaunchRequestLocation)
            MainActivityPermissionsDispatcher.beginLocationUpdatesWithPermissionCheck(this);
    }

    @Override
    protected void onStop()
    {
        stopLocationUpdates();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        outState.putBoolean(LOCATION_UPDATES_STATE_KEY, canLaunchRequestLocation);
        super.onSaveInstanceState(outState);
    }

    // =================================
    //              Methods
    // =================================

    private void restoreValuesInBundle(Bundle state)
    {
        if (state == null)
            return;

        if (state.keySet().contains(LOCATION_UPDATES_STATE_KEY))
            canLaunchRequestLocation = state.getBoolean(LOCATION_UPDATES_STATE_KEY, false);
    }

    private void configureUI()
    {
        binding.activityMainLogsText.setMovementMethod(new ScrollingMovementMethod());
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void beginLocationUpdates()
    {
        ExtensionsKt.shortToast(MainActivity.this, R.string.activity_main_permission_granted_msg);
        configurePermissionStatusImg(true);

        locationCallback = new LocationCallback()
        {
            private int count = 0;
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if (locationResult == null)
                    return;

                float speed = locationResult.getLastLocation().getSpeed();
                binding.activityMainSpeedText.setText(getString(R.string.activity_main_speed_text, speed));
                binding.activityMainLogsText.append(++count + ". " + getString(R.string.activity_main_speed_text, speed) + "\n");
                ExtensionsKt.errorLog(MainActivity.this, "onLocationResult: " + speed, null);
                super.onLocationResult(locationResult);
            }
        };

        configureLocationRequest();
        checkIfLocationSettingIsEnabled();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showLocationRationale(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setMessage("To use this app you must grant location access")
                .setPositiveButton(R.string.activity_main_dialog_positive_btn, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.activity_main_dialog_negative_btn, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void onPermissionDenied()
    {
        configurePermissionStatusImg(false);
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onNeverAskPermission()
    {
        configurePermissionStatusImg(null);
    }

    private void configurePermissionStatusImg(Boolean isLocationGranted)
    {
        if (isLocationGranted == null)
        {
            binding.activityMainPermissionInfoImg.setImageResource(R.drawable.ic_baseline_error_outline_24);
            binding.activityMainPermissionInfoText.setText(R.string.activity_main_never_ask_permission_msg);
            return;
        }

        int backgroundReference = isLocationGranted ? R.drawable.ic_baseline_check_circle_outline_24 : R.drawable.ic_baseline_error_outline_24;
        int textReference = isLocationGranted ? R.string.activity_main_permission_granted_msg : R.string.activity_main_permission_denied_msg;
        binding.activityMainPermissionInfoImg.setImageResource(backgroundReference);
        binding.activityMainPermissionInfoText.setText(textReference);
    }

    // --- Location --- //

    private void configureLocationRequest()
    {
        request = LocationRequest.create();
        request.setInterval(UPDATE_INTERVAL_MILLIS);
        request.setFastestInterval(FAsTEST_UPDATE_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkIfLocationSettingIsEnabled()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse ->
                        {
                            ExtensionsKt.errorLog(this, "Location enabled", null);
                            canLaunchRequestLocation = true;
                            startLocationUpdates();
                        }
                )
                .addOnFailureListener((e) ->
                {
                    if (e instanceof ResolvableApiException)
                    {
                        try
                        {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx)
                        {
                            ExtensionsKt.errorLog(MainActivity.this, "An error occurred when sending location setting intent", sendEx);
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates()
    {
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates()
    {
        if (fusedLocationClient == null)
            return;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}