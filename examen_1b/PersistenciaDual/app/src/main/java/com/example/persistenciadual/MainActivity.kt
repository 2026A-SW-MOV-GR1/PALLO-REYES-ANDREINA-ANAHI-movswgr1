package com.example.persistenciadual

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.persistenciadual.databinding.ActivityMainBinding
import com.example.persistenciadual.ui.SongListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SongListFragment())
                .commit()
        }
    }
}