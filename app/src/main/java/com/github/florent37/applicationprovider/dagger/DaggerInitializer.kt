package com.github.florent37.applicationprovider.dagger

import com.github.florent37.application.provider.ApplicationProvider

lateinit var graph: AppComponent

val InitializeDagger by lazy {
    ApplicationProvider.listen { application ->
        graph = DaggerAppComponent
            .factory()
            .create(application)
    }
}