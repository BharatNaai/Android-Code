package com.app.bharatnaai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.bharatnaai.ui.home.HomeFragment
import com.app.bharatnaai.ui.profile.UserProfileFragment
import com.app.bharatnaai.ui.search.SearchFragment
import bharatnaai.R
import bharatnaai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        setupNavigation()
        // Load home fragment by default and set home as selected
        loadFragment(HomeFragment.newInstance())
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment.newInstance())
                    true
                }
                R.id.nav_bookings -> {
                    // TODO: Create BookingsFragment
                    Toast.makeText(this, "Bookings - Coming soon!", Toast.LENGTH_SHORT).show()
                    // loadFragment(BookingsFragment.newInstance())
                    false // Don't select until fragment is created
                }
                R.id.nav_profile -> {
                    loadFragment(UserProfileFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onStart() {
        super.onStart()
    }

}