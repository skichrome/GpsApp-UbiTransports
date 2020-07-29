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
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.skichrome.gpsapp.BuildConfig;
import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.ActivityMainBinding;
import com.skichrome.gpsapp.model.local.database.RoomLocation;
import com.skichrome.gpsapp.service.GpsLocationService;
import com.skichrome.gpsapp.util.ExtensionsKt;
import com.skichrome.gpsapp.viewmodel.ActivityMainViewModel;

import java.util.List;

import kotlin.Lazy;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static org.koin.java.KoinJavaComponent.inject;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    // =================================
    //              Fields
    // =================================

    private ActivityMainBinding binding;
    private Lazy<ActivityMainViewModel> viewModelLazy = inject(ActivityMainViewModel.class);

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
        configureViewModel();
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

    private void configureViewModel()
    {
        viewModelLazy.getValue().sayHello();
        viewModelLazy.getValue().getLocation().observe(this, locations ->
        {
            if (locations == null || locations.isEmpty())
            {
                ExtensionsKt.errorLog(MainActivity.this, "Location list is empty or null", null);
                return;
            }
            handleLocationResult(locations);
            ExtensionsKt.errorLog(MainActivity.this, "New Location available ! id: " + locations.get(locations.size() - 1).getId(), null);
        });
    }

    private void handleLocationResult(List<RoomLocation> locations)
    {
        RoomLocation lastLocation = locations.get(locations.size() - 1);
        float speed = lastLocation.getSpeed() * 3.6f;
        int intSpeed = Math.round(speed);
        if (intSpeed <= 100 && intSpeed > 0)
            updateUI(intSpeed);
        if (intSpeed > 100)
            updateUI(100);
        if (intSpeed <= 0)
            updateUI(1);

        binding.activityMainLogsText.append(getString(R.string.activity_main_speed_text, Math.round(speed)) + "\n");
        binding.activityMainLogsScrollView.fullScroll(View.FOCUS_DOWN);
        ExtensionsKt.errorLog(MainActivity.this, "onLocationResult: " + speed + " km/h (=>" + intSpeed + ")", null);
    }

    private void updateUI(int percent)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        binding.activityMainSpeedBar.getLayoutParams().width = percent * metrics.widthPixels / 100;
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
        configurePermissionStatusImg(true);
        bindService(new Intent(MainActivity.this, GpsLocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
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