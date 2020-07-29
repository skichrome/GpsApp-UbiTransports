package com.skichrome.gpsapp

import com.skichrome.gpsapp.model.DefaultLocationRepository
import com.skichrome.gpsapp.model.LocationRepository
import com.skichrome.gpsapp.viewmodel.ActivityMainViewModel
import com.skichrome.gpsapp.viewmodel.GpsLocationServiceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val appModule = module {
    single<LocationRepository> { DefaultLocationRepository() }

    viewModel { ActivityMainViewModel(get()) }
    viewModel { GpsLocationServiceViewModel(get()) }
}