package com.github.florent37.applicationprovider

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.applicationprovider.dagger.InitializeDagger
import com.github.florent37.applicationprovider.java.PreferencesManager
import com.github.florent37.applicationprovider.stetho.InitializeStetho
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    init {
        InitializeStetho
        InitializeDagger
    }

    private var preferencesManager = PreferencesManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.textView).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
