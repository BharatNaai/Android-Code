package com.app.bharatnaai.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bharatnaai.R
import bharatnaai.databinding.FragmentHomeBinding
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.ui.search.SearchFragment
import com.app.bharatnaai.ui.notifications.NotificationFragment
import com.app.bharatnaai.ui.saloon_details.SaloonDetailsFragment
import com.app.bharatnaai.utils.LocationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (granted) {
            viewModel.fetchNearbySalonsByLocation()
        }
    }

    private lateinit var featuredSalonsAdapter: FeaturedSalonsAdapter
    private lateinit var exclusiveOffersAdapter: ExclusiveOffersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        setupClickListeners()
        ensureLocationPermission()
        observeData()
    }

    private fun setupViews() {
        // Setup any initial view configurations
    }

    private fun setupRecyclerView() {
        // Featured Salons Adapter
        featuredSalonsAdapter = FeaturedSalonsAdapter(
            onSalonClick = { salon ->
                navigateToSalonDetails(salon)
            }
        )

        binding.rvFeaturedSalons.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredSalonsAdapter
        }

        // Exclusive Offers Adapter
        exclusiveOffersAdapter = ExclusiveOffersAdapter { offer ->
            Toast.makeText(context, "Selected offer: ${offer.title}", Toast.LENGTH_SHORT).show()
        }

        binding.rvExclusiveOffers.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = exclusiveOffersAdapter
        }
    }

    private fun observeData() {
        viewModel.homeState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }

        viewModel.featuredSalons.observe(viewLifecycleOwner) { salons ->
            featuredSalonsAdapter.submitList(salons)
        }

        viewModel.exclusiveOffers.observe(viewLifecycleOwner) { offers ->
            exclusiveOffersAdapter.submitList(offers)
        }
    }

    private fun updateUI(state: HomeState) {
        // Handle loading states, errors, etc.
        
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun navigateToSalonDetails(salon: Salon) {
        val fragment = SaloonDetailsFragment()
        fragment.arguments = Bundle().apply {
            putInt("salonId", salon.salonId)
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Assuming you have a container with this ID
            .addToBackStack(null)
            .commit()
    }

    private fun setupClickListeners() {

        // Notification click - navigate to notifications screen
        binding.notificationContainer.setOnClickListener {
            val notificationFragment = NotificationFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, notificationFragment)
                .addToBackStack(null)
                .commit()
        }

        // Search functionality
        binding.searchBar.setOnClickListener {
            navigateToSearchFrag()
        }

        binding.tvViewAll.setOnClickListener {
            navigateToSearchFrag()
        }

        // Note: Exclusive offers now handled by RecyclerView adapter

        // Service clicks
        binding.serviceHaircut.setOnClickListener {
            Toast.makeText(context, "Haircut services - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.serviceShaving.setOnClickListener {
            Toast.makeText(context, "Shaving services - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.serviceGrooming.setOnClickListener {
            Toast.makeText(context, "Grooming services - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.servicePackages.setOnClickListener {
            Toast.makeText(context, "Package deals - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun ensureLocationPermission() {
        val fine = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarse = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.fetchNearbySalonsByLocation()
        }
    }

    private fun requestLocationPermission() {
        when {
            viewModel.homeState.value?.hasLocationPermission == true -> {
                // Permission already granted, get location
                viewModel.fetchNearbySalonsByLocation()
            }

            shouldShowRequestPermissionRationale(LocationHelper.REQUIRED_PERMISSIONS[0]) -> {
                // Show rationale dialog
                showPermissionRationaleDialog()
            }

            else -> {
                // Request permission directly
                locationPermissionLauncher.launch(LocationHelper.REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show nearby salons and services. Please grant the permission to continue.")
            .setPositiveButton("Grant Permission") { _, _ ->
                locationPermissionLauncher.launch(LocationHelper.REQUIRED_PERMISSIONS)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    context,
                    "Location permission is required to get your current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun handlePermissionDenied() {
        val shouldShowRationale = LocationHelper.REQUIRED_PERMISSIONS.any { permission ->
            shouldShowRequestPermissionRationale(permission)
        }

        if (!shouldShowRationale) {
            // Permission permanently denied, show settings dialog
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Location permission is permanently denied. Please enable it from app settings to get your current location.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance() = HomeFragment()
    }

    fun navigateToSearchFrag() {
        val searchfrag = SearchFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, searchfrag)
            .addToBackStack(null)
            .commit()

        // ✅ Update BottomNavigationView to highlight the Search tab
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_search
    }
}
