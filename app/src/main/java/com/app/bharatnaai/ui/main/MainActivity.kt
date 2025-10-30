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
import com.app.bharatnaai.ui.my_booking.BookingHistoryFrag
import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        // Enable edge-to-edge so we can manage insets ourselves
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make system bars transparent for seamless look
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Apply bottom inset to BottomNavigationView only on devices that need it (gesture nav)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

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
                    loadFragment(BookingHistoryFrag.newInstance())
                    true
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
                    PreferenceManager.saveUserName(this, customerData.fullName)
                    PreferenceManager.saveUserEmail(this, customerData.email)
                    PreferenceManager.saveUserPhone(this, customerData.phone)
                    PreferenceManager.saveUserId(this, customerData.userId)
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