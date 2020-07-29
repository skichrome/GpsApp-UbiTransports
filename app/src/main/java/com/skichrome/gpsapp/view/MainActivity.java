package com.skichrome.gpsapp.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.skichrome.gpsapp.BuildConfig;
import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.ActivityMainBinding;
import com.skichrome.gpsapp.service.GpsLocationService;

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

    private GpsLocationService service;
    private boolean bound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            GpsLocationService.LocalBinder binder = (GpsLocationService.LocalBinder) iBinder;
            service = binder.getService();
            bound = true;
            service.startLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            service = null;
            bound = false;
        }
    };

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        checkIfLocationSettingIsEnabled();
    }

    @Override
    protected void onStop()
    {
        if (bound)
        {
            unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    // =================================
    //              Methods
    // =================================

    // --- UI & configuration --- //

    private NavController getNavController()
    {
        return Navigation.findNavController(this, R.id.activity_main_nav_host_fragment);
    }

    private void configureToolbar(NavController navController)
    {
        NavigationUI.setupWithNavController(binding.toolbar, navController);
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
        bindService(new Intent(MainActivity.this, GpsLocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        // Configure UI after service init
        NavController navController = getNavController();
        configureToolbar(navController);
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
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_main_dialog_ask_settings_title)
                .setMessage(R.string.activity_main_dialog_ask_settings_message)
                .setPositiveButton(R.string.activity_main_dialog_ask_settings_positive_btn, ((dialogInterface, i) -> askToGoToSettings()))
                .setNegativeButton(R.string.activity_main_dialog_ask_settings_negative_btn, ((dialogInterface, i) -> MainActivity.this.finish()))
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onNeverAskPermission()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_main_dialog_never_ask_settings_title)
                .setMessage(R.string.activity_main_dialog_never_ask_settings_message)
                .setPositiveButton(R.string.activity_main_dialog_never_ask_settings_positive_btn, ((dialogInterface, i) -> askToGoToSettings()))
                .setNegativeButton(R.string.activity_main_dialog_never_ask_settings_negative_btn, ((dialogInterface, i) -> MainActivity.this.finish()))
                .show();
    }

    // --- Location Configuration --- //

    private void checkIfLocationSettingIsEnabled()
    {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null)
            return;

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.activity_main_dialog_ask_enable_location_param_title)
                    .setMessage(R.string.activity_main_dialog_ask_enable_location_param_message)
                    .setPositiveButton(R.string.activity_main_dialog_ask_enable_location_param_positive_btn, ((dialog, i) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))))
                    .setNegativeButton(R.string.activity_main_dialog_ask_enable_location_param_negative_btn, (dialogInterface, i) -> finish())
                    .create()
                    .show();
        } else
            MainActivityPermissionsDispatcher.beginLocationUpdatesWithPermissionCheck(this);
    }
}