package com.app.bharatnaai.ui.my_booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bharatnaai.R
import bharatnaai.databinding.FragmentBookingHistoryBinding
import com.app.bharatnaai.utils.PreferenceManager

class BookingHistoryFrag : Fragment() {

    private var _binding: FragmentBookingHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingHistoryViewModel by viewModels()
    private lateinit var adapter: BookingHistoryAdapter

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

        // Fetch bookings for current user
        PreferenceManager.getUserId(requireContext())?.let { uid ->
            viewModel.fetch(uid)
        }
    }

    private fun setupUI() {
        // default select Upcoming
        binding.toggleSegments.check(binding.btnUpcoming.id)
        updateToggleUI(binding.btnUpcoming.id)

        binding.toggleSegments.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.btnUpcoming.id -> viewModel.setTab(BookingStatus.UPCOMING)
                binding.btnCompleted.id -> viewModel.setTab(BookingStatus.COMPLETED)
            }

            updateToggleUI(checkedId)
        }
    }

    private fun updateToggleUI(checkedId: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.button_primary_end)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.notification_background)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        if (checkedId == binding.btnUpcoming.id) {
            // Upcoming selected
            binding.btnUpcoming.setBackgroundColor(selectedColor)
            binding.btnUpcoming.setTextColor(selectedTextColor)

            binding.btnCompleted.setBackgroundColor(unselectedColor)
            binding.btnCompleted.setTextColor(unselectedTextColor)

        } else if (checkedId == binding.btnCompleted.id) {
            // Completed selected
            binding.btnCompleted.setBackgroundColor(selectedColor)
            binding.btnCompleted.setTextColor(selectedTextColor)

            binding.btnUpcoming.setBackgroundColor(unselectedColor)
            binding.btnUpcoming.setTextColor(unselectedTextColor)
        }
    }

    private fun setupRecycler() {
        adapter = BookingHistoryAdapter(object : BookingHistoryAdapter.Listener {
            override fun onViewDetails(item: BookingItem) { /* TODO: navigate to detail */ }
            override fun onReschedule(item: BookingItem) { /* TODO: reschedule flow */ }
            override fun onRebook(item: BookingItem) { /* TODO: rebook flow */ }
            override fun onReview(item: BookingItem) { /* TODO: review flow */ }
            override fun onOverflow(anchor: View, item: BookingItem) {
                val popup = PopupMenu(requireContext(), anchor)
                popup.menu.add(0, 1, 0, "Help")
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId == 1) {
                        navigateToHelp(item)
                        true
                    } else false
                }
                popup.show()
            }
        })
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookingHistoryFrag.adapter
            setHasFixedSize(false)
        }
    }

    private fun navigateToHelp(item: BookingItem) {
        // TODO: replace with actual Help screen navigation; passing bookingId if required
        // Example when HelpFragment is available:
        // val fragment = HelpFragment().apply { arguments = Bundle().apply { putString("bookingId", item.id) } }
        // parentFragmentManager.beginTransaction()
        //     .replace(bharatnaai.R.id.fragment_container, fragment)
        //     .addToBackStack(null)
        //     .commit()
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) { items -> adapter.submitList(items) }
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { _ -> /* show loader if needed */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
