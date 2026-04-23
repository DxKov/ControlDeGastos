package com.example.controldegastos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for GastoApp.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class GastoApp : Application()
