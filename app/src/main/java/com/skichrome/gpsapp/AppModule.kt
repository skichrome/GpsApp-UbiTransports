package com.skichrome.gpsapp

import androidx.room.Room
import com.skichrome.gpsapp.model.DefaultEditLocationRepository
import com.skichrome.gpsapp.model.DefaultReadLocationRepository
import com.skichrome.gpsapp.model.base.EditLocationRepository
import com.skichrome.gpsapp.model.base.LocationSource
import com.skichrome.gpsapp.model.base.ReadLocationRepository
import com.skichrome.gpsapp.model.local.LocalLocationSource
import com.skichrome.gpsapp.model.local.database.GpsAppDatabase
import com.skichrome.gpsapp.model.local.database.LocationDao
import com.skichrome.gpsapp.viewmodel.ActivityMainViewModel
import com.skichrome.gpsapp.viewmodel.GpsLocationServiceViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val appModule = module {
    // Local Database
    single {
        Room.databaseBuilder(androidApplication(), GpsAppDatabase::class.java, "gps-app-database.db")
                .build()
    }
    single<LocationDao> { get<GpsAppDatabase>().locationDao() }

    // Sources
    single<LocationSource> { LocalLocationSource(get()) }

    // Repositories
    single<ReadLocationRepository> { DefaultReadLocationRepository(get()) }
    single<EditLocationRepository> { DefaultEditLocationRepository(get()) }

    // ViewModels
    viewModel { ActivityMainViewModel(get()) }
    viewModel { GpsLocationServiceViewModel(get()) }
}