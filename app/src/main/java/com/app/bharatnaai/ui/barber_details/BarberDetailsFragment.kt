package com.app.bharatnaai.ui.barber_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import bharatnaai.databinding.FragmentBarberDetailsBinding
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BarberDetailsFragment : Fragment() {

    private var _binding: FragmentBarberDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BarberDetailsViewModel by viewModels()

    private lateinit var timeSlotsAdapter: TimeSlotsAdapter
    private val selectedServices = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarberDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Barber dropdown cycles selection for now
        setupTimeRecycler()
        setupServicesGrid()
        observeViewModel()

        // Read barberId and set default date -> fetch slots
        val barberId = arguments?.getInt("barberId")
        val barberName = arguments?.getString("barberName")
        if (!barberName.isNullOrBlank()) {
            binding.tvBarberName.text = barberName
        }
        setupDatePicker(barberId)
        if (barberId != null) {
            val apiDate = todayApiDate()
            binding.tvDate.text = todayDisplayDate()
            viewModel.fetchSlots(barberId, apiDate)
        }

        binding.llPromo.setOnClickListener {
            // Toggle promo as a demo; replace with input bottom sheet later
            val current = viewModel.state.value?.promoCode
            viewModel.applyPromo(if (current == null) "SAVE10" else null)
            val label = if (current == null) "Promo SAVE10 applied" else "Promo removed"
            Toast.makeText(requireContext(), label, Toast.LENGTH_SHORT).show()
        }

        binding.btnBook.setOnClickListener {
        }
    }

    private fun setupTimeRecycler() {
        timeSlotsAdapter = TimeSlotsAdapter { index -> viewModel.selectTimeSlot(index) }
        binding.rvTimeSlots.layoutManager = GridLayoutManager(requireContext(), 3, LinearLayoutManager.VERTICAL, false)
        binding.rvTimeSlots.adapter = timeSlotsAdapter
    }

    private fun setupServicesGrid() {
        // Map views to service ids
        val svcViews = listOf(
            "haircut" to binding.serviceHaircut,
            "shaving" to binding.serviceShaving,
            "grooming" to binding.serviceGrooming,
        )

        svcViews.forEach { (id, view) ->
            // initialize default state
            applyServiceSelection(view, selectedServices.contains(id))
            view.setOnClickListener {
                if (selectedServices.contains(id)) selectedServices.remove(id) else selectedServices.add(id)
                applyServiceSelection(view, selectedServices.contains(id))
            }
        }
    }

    private fun applyServiceSelection(target: View, selected: Boolean) {
        val drawable = if (selected) {
            ContextCompat.getDrawable(requireContext(), bharatnaai.R.drawable.filter_chip_selected_background)
        } else {
            ContextCompat.getDrawable(requireContext(), bharatnaai.R.drawable.card_background)
        }
        target.background = drawable
    }

    private fun setupDatePicker(barberId: Int?) {
        binding.tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val apiDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    val disp = String.format(Locale.US, "%02d / %02d / %04d", d, m + 1, y)
                    binding.tvDate.text = disp
                    barberId?.let { viewModel.fetchSlots(it, apiDate) }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dlg.datePicker.minDate = System.currentTimeMillis() - 1000
            dlg.show()
        }
    }

    private fun todayApiDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Calendar.getInstance().time)
    }

    private fun todayDisplayDate(): String {
        val cal = Calendar.getInstance()
        return String.format(Locale.US, "%02d / %02d / %04d",
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { s ->
            binding.swipeRefresh.isRefreshing = s.isLoading
            binding.tvTitle.text = "Book Appointment"

            timeSlotsAdapter.submitList(s.timeSlots)
            timeSlotsAdapter.selectedIndex = s.selectedTimeIndex

            // Empty state handling
            val hasSlots = s.timeSlots.isNotEmpty()
            binding.rvTimeSlots.visibility = if (hasSlots) View.VISIBLE else View.GONE
            binding.tvNoSlots.visibility = if (!hasSlots && !s.isLoading) View.VISIBLE else View.GONE
        }

        viewModel.barbers.observe(viewLifecycleOwner) {
            // could populate a popup list later; for now we cycle on tap
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
