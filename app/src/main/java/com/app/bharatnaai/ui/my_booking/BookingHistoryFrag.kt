package com.app.bharatnaai.ui.my_booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bharatnaai.databinding.FragmentBookingHistoryBinding

class BookingHistoryFrag : Fragment() {

    private var _binding: FragmentBookingHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingHistoryViewModel by viewModels()
    private lateinit var adapter: BookingSuccessAdapter

    companion object { fun newInstance() = BookingHistoryFrag() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecycler()
        observeViewModel()
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun setupRecycler() {
        adapter = BookingSuccessAdapter { /* on item click -> could open booking detail */ }
        binding.rvBookingSuccess.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookingHistoryFrag.adapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            adapter.submitList(sections)
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvBookingSuccess.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { _ ->
            // Optional: show loader
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
