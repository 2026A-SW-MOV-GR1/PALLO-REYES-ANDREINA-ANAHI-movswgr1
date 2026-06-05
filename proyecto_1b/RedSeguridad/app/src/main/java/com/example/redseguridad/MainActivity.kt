package com.example.redseguridad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.redseguridad.databinding.ActivityMainBinding
import com.example.redseguridad.ui.rest.RestApiFragment
import com.example.redseguridad.ui.secrets.SecretsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carga el primer fragment solo en la creación inicial,
        // no en recreaciones por rotación de pantalla.
        if (savedInstanceState == null) {
            loadFragment(RestApiFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_rest
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_rest     -> RestApiFragment()
                R.id.nav_secrets  -> SecretsFragment()
                else              -> RestApiFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}