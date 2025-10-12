package com.app.bharatnaai.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.bharatnaai.ui.home.HomeFragment
import com.app.bharatnaai.ui.profile.UserProfileFragment
import com.app.bharatnaai.ui.search.SearchFragment
import com.app.bharatnaai.utils.PreferenceManager
import bharatnaai.R
import bharatnaai.databinding.ActivityMainBinding
import com.app.bharatnaai.data.repository.ApiResult

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        setupNavigation()
        loadFragment(HomeFragment.newInstance())
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        viewModel.fetchCustomerDetails()
        observeCustomerDetails()
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
                    Toast.makeText(this, "Bookings - Coming soon!", Toast.LENGTH_SHORT).show()
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

    private fun observeCustomerDetails() {
        viewModel.customerDetailsState.observe(this) { result ->
            when (result) {
                is ApiResult.Success -> {
                    val customerData = result.data
                    PreferenceManager.saveUserName(this, customerData.name)
                    PreferenceManager.saveUserEmail(this, customerData.email)
                    PreferenceManager.saveUserPhone(this, customerData.phone)
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is ApiResult.Loading -> {
                    // Optionally, show a loading indicator
                }
            }
        }
    }

}