package com.app.bharatnaai.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bharatnaai.ui.saloon_details.SaloonDetailsFragment
import bharatnaai.R
import bharatnaai.databinding.FragmentSearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var salonAdapter: SalonSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        setupClickListeners()
        observeData()
    }

    private fun setupViews() {
        // Set initial search text
        binding.etSearch.setText("Haircut")
        binding.etSearch.setSelection(binding.etSearch.text?.length ?: 0)
    }

    private fun setupRecyclerView() {
        salonAdapter = SalonSearchAdapter(
            onSalonClick = { salon ->
                navigateToSalonDetails(salon)
            },
            onBookNowClick = { salon ->
                navigateToSalonDetails(salon)
            }
        )

        binding.rvSalons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = salonAdapter
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Clear search
        binding.ivClear.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.clearSearch()
        }

        // Search text watcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.updateSearchQuery(query)
            }
        })

        // Filter chips
        binding.filterDistance.setOnClickListener {
            viewModel.toggleFilter(FilterType.DISTANCE)
        }

        binding.filterRating.setOnClickListener {
            viewModel.toggleFilter(FilterType.RATING)
        }

        binding.filterPrice.setOnClickListener {
            viewModel.toggleFilter(FilterType.PRICE)
        }

        binding.filterService.setOnClickListener {
            viewModel.toggleFilter(FilterType.SERVICE)
        }
    }

    private fun observeData() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: SearchState) {
        // Update salon list
        salonAdapter.submitList(state.salons)

        // Update filter chips appearance
        updateFilterChips(state.filters)

        // Handle loading state
        // You can add a progress indicator here if needed

        // Handle errors
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun navigateToSalonDetails(salon: Salon) {
        val fragment = SaloonDetailsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Assuming you have a container with this ID
            .addToBackStack(null)
            .commit()
    }

    private fun updateFilterChips(filters: List<SearchFilter>) {
        filters.forEach { filter ->
            val chipView = when (filter.type) {
                FilterType.DISTANCE -> binding.filterDistance
                FilterType.RATING -> binding.filterRating
                FilterType.PRICE -> binding.filterPrice
                FilterType.SERVICE -> binding.filterService
            }

            if (filter.isSelected) {
                chipView.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.filter_chip_selected_background
                )
                chipView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                chipView.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.filter_chip_background
                )
                chipView.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_text_color))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}
