package com.skichrome.gpsapp;

import android.app.Application;

import org.koin.android.java.KoinAndroidApplication;
import org.koin.core.KoinApplication;
import org.koin.core.context.GlobalContext;

import static org.koin.core.context.ContextFunctionsKt.startKoin;

public class GpsAppApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Koin starting
        KoinApplication koin = KoinAndroidApplication.create(this)
                .printLogger()
                .modules(AppModuleKt.appModule);
        startKoin(new GlobalContext(), koin);
    }
}