package com.anas.notefolio

import android.app.Application
import com.anas.notefolio.data.NoteRepository
import com.anas.notefolio.data.SecurityRepository
import com.anas.notefolio.data.SettingsRepository
import com.anas.notefolio.data.local.AppDatabase

class NoteFolioApp : Application() {
    lateinit var repository: NoteRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var securityRepository: SecurityRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = NoteRepository(db.noteDao(), db.folderDao())
        settingsRepository = SettingsRepository(this)
        securityRepository = SecurityRepository(this)
    }
}
