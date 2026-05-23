package com.indoora.app

import android.app.Application

// Contexto global de la app

class IndooraApplication : Application() {
    companion object {
        lateinit var instance: IndooraApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}