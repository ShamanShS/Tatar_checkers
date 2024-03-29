package com.shamanshs.tatar_checkers

import android.app.Application
import com.google.android.material.color.DynamicColors

class TipTimeApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}