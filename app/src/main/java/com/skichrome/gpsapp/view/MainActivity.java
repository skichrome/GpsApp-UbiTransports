package com.skichrome.gpsapp.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;

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
import com.skichrome.gpsapp.BuildConfig;
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
    private static final int FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL_MILLIS / 2;
    private static final int SLOWEST_UPDATE_INTERVAL = UPDATE_INTERVAL_MILLIS * 2;
    private static final int REQUEST_CHECK_SETTINGS = 1234;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        MainActivityPermissionsDispatcher.beginLocationUpdatesWithPermissionCheck(this);
    }

    @Override
    protected void onStop()
    {
        stopLocationUpdates();
        super.onStop();
    }

    // =================================
    //              Methods
    // =================================

    // --- UI & configuration --- //

    private void updateUI(int percent)
    {
        binding.activityMainSpeedBar.getLayoutParams().width = percent;
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

    private void askToGoToSettings()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // --- Permissions-linked methods --- //

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void beginLocationUpdates()
    {
        configureLocationCallback();
        configurePermissionStatusImg(true);
        configureLocationRequest();
        checkIfLocationSettingIsEnabled();
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showLocationRationale(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_main_dialog_show_perm_request_title)
                .setMessage(R.string.activity_main_dialog_show_perm_request_message)
                .setPositiveButton(R.string.activity_main_dialog_show_perm_request_positive_btn, ((dialogInterface, i) -> request.proceed()))
                .setNegativeButton(R.string.activity_main_dialog_show_perm_request_negative_btn, ((dialogInterface, i) -> request.cancel()))
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

        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_main_dialog_ask_settings_title)
                .setMessage(R.string.activity_main_dialog_ask_settings_message)
                .setPositiveButton(R.string.activity_main_dialog_ask_settings_positive_btn, ((dialogInterface, i) -> askToGoToSettings()))
                .setNegativeButton(R.string.activity_main_dialog_ask_settings_negative_btn, ((dialogInterface, i) -> MainActivity.this.finish()))
                .show();
    }

    // --- Location Configuration --- //

    private void configureLocationCallback()
    {
        locationCallback = new LocationCallback()
        {
            private int count = 0;

            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if (locationResult == null)
                    return;
                handleLocationResult(locationResult, ++count);
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

    private void checkIfLocationSettingIsEnabled()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);

        SettingsClient client = LocationServices.getSettingsClient(this);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse ->
                        {
                            ExtensionsKt.errorLog(this, "Location enabled", null);
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

    private void handleLocationResult(LocationResult locationResult, int count)
    {
        float speed = locationResult.getLastLocation().getSpeed() * 3.6f;
        int intSpeed = Math.round(speed);
        if (intSpeed <= 100 && intSpeed > 0)
            updateUI(intSpeed);
        if (intSpeed > 100)
            updateUI(100);
        if (intSpeed <= 0)
            updateUI(1);

        binding.activityMainLogsText.append(count + ". " + getString(R.string.activity_main_speed_text, Math.round(speed)) + "\n");
        binding.activityMainLogsScrollView.fullScroll(View.FOCUS_DOWN);
        ExtensionsKt.errorLog(MainActivity.this, "onLocationResult: " + speed + " km/h (=>" + intSpeed + ")", null);
    }

    private void stopLocationUpdates()
    {
        if (fusedLocationClient == null)
            return;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}