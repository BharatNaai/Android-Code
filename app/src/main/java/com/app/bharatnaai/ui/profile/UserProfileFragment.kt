package com.app.bharatnaai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bharatnaai.ui.auth.login.LoginActivity
import bharatnaai.databinding.FragmentUserProfileBinding

class UserProfileFragment : Fragment() {
    
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UserProfileViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(UserProfileViewModel::class.java)
    }
    private lateinit var bookingHistoryAdapter: BookingHistoryAdapter
    private lateinit var savedSalonsAdapter: SavedSalonsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupRecyclerViews()
        observeData()
        setupClickListeners()
    }
    
    private fun setupViews() {
        // Setup any initial view configurations
    }
    
    private fun setupRecyclerViews() {
        // Booking History Adapter
        bookingHistoryAdapter = BookingHistoryAdapter { booking ->
            Toast.makeText(context, "Booking: ${booking.salonName}", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvBookingHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookingHistoryAdapter
        }
        
        // Saved Salons Adapter
        savedSalonsAdapter = SavedSalonsAdapter(
            onSalonClick = { salon ->
                Toast.makeText(context, "Salon: ${salon.name}", Toast.LENGTH_SHORT).show()
            },
            onBookmarkClick = { salon ->
                viewModel.removeSavedSalon(salon)
                Toast.makeText(context, "Removed ${salon.name} from saved", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvSavedSalons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = savedSalonsAdapter
        }
    }
    
    private fun observeData() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
        
        viewModel.bookingHistory.observe(viewLifecycleOwner) { bookings ->
            bookingHistoryAdapter.submitList(bookings)
        }
        
        viewModel.savedSalons.observe(viewLifecycleOwner) { salons ->
            savedSalonsAdapter.submitList(salons)
        }
    }
    
    private fun updateUI(state: ProfileState) {
        if (state.isLoggedIn) {
            // Show logged-in content
            binding.loggedInContent.visibility = View.VISIBLE
            binding.loggedOutContent.visibility = View.GONE
            
            // Update user profile info
            state.userProfile?.let { profile ->
                binding.tvUserName.text = profile.name
                binding.tvPhoneNumber.text = profile.phoneNumber
                binding.tvEmail.text = profile.email
                // TODO: Load profile image using Glide/Picasso
            }
        } else {
            // Show logged-out content
            binding.loggedInContent.visibility = View.GONE
            binding.loggedOutContent.visibility = View.VISIBLE
        }
        
        // Handle loading states, errors, etc.
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            // Handle back navigation - could close fragment or navigate back
            requireActivity().onBackPressed()
        }
        
        // Edit button
        binding.btnEdit.setOnClickListener {
            Toast.makeText(context, "Edit profile - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Logout button
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            
            // Navigate to LoginActivity and clear activity stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
        
        // Login button (in logged-out state)
        binding.btnProfileLogin.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = UserProfileFragment()
    }
}
