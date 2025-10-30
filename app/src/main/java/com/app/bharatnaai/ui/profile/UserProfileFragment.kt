package com.app.bharatnaai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import bharatnaai.R
import com.app.bharatnaai.ui.auth.login.LoginActivity
import bharatnaai.databinding.FragmentUserProfileBinding
import com.app.bharatnaai.utils.PreferenceManager
import com.app.bharatnaai.ui.my_booking.BookingHistoryFrag
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )
            .get(UserProfileViewModel::class.java)
    }

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
        observeData()
        setupClickListeners()

        val initialTop = binding.topBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = initialTop + systemBars.top)
            insets
        }
    }

    private fun setupViews() {
        // Setup any initial view configurations
    }

    private fun observeData() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: ProfileState) {
        if (state.isLoggedIn) {
            // Show logged-in content
            binding.loggedInContent.visibility = View.VISIBLE
            binding.loggedOutContent.visibility = View.GONE

            val context = requireContext()
            binding.tvUserName.text = PreferenceManager.getUserName(context) ?: "N/A"
            binding.tvEmail.text = PreferenceManager.getUserEmail(context) ?: "N/A"
            binding.tvPhoneNumber.text = PreferenceManager.getUserPhone(context) ?: "N/A"
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
        // Menu: My Bookings
        binding.includeProfileMenu.llMyBookings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookingHistoryFrag.newInstance())
                .addToBackStack(null)
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_bookings
        }
        // Menu: Favorites
        binding.includeProfileMenu.llMyFavorite.setOnClickListener {
            Toast.makeText(requireContext(), "Favorites coming soon", Toast.LENGTH_SHORT).show()
        }
        // Menu: Payment Methods
        binding.includeProfileMenu.llMyPayment.setOnClickListener {
            Toast.makeText(requireContext(), "Payment methods coming soon", Toast.LENGTH_SHORT).show()
        }
        // Menu: Settings
        binding.includeProfileMenu.llMySetting.setOnClickListener {
            Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show()
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // Login button (logged-out)
        binding.btnProfileLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = UserProfileFragment()
    }
}
