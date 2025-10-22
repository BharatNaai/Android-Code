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
import com.app.bharatnaai.ui.custom_dialog.BookingConfirmDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BarberDetailsFragment : Fragment() {

    private var _binding: FragmentBarberDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BarberDetailsViewModel by viewModels()

    private lateinit var timeSlotsAdapter: TimeSlotsAdapter
    private var selectedService: String? = null

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
            val idx = viewModel.state.value?.selectedTimeIndex ?: -1
            val slots = viewModel.state.value?.timeSlots.orEmpty()
            if (idx < 0 || idx >= slots.size) {
                Toast.makeText(requireContext(), "Please select a time slot", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val slot = slots[idx]

            // Format date: 20 October 2025
            val dateLabel = try {
                val input = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(slot.slotDate)
                SimpleDateFormat("d MMMM yyyy", Locale.US).format(input!!)
            } catch (e: Exception) {
                slot.slotDate
            }

            // Format time: 09:40 AM
            val timeLabel = try {
                val tin = SimpleDateFormat("HH:mm:ss", Locale.US).parse(slot.startTime)
                SimpleDateFormat("hh:mm a", Locale.US).format(tin!!)
            } catch (e: Exception) {
                slot.startTime
            }

            val confirmNo =
                "#BK" + (100000 + kotlin.random.Random.Default.nextInt(900000)).toString()

            val dialog = BookingConfirmDialogFragment.newInstance(
                confirmNo = confirmNo,
                date = dateLabel,
                time = timeLabel
            )
            dialog.show(parentFragmentManager, "booking_confirm_dialog")
        }
    }

    private fun setupTimeRecycler() {
        timeSlotsAdapter = TimeSlotsAdapter { index -> viewModel.selectTimeSlot(index) }
        binding.rvTimeSlots.layoutManager =
            GridLayoutManager(requireContext(), 3, LinearLayoutManager.VERTICAL, false)
        binding.rvTimeSlots.adapter = timeSlotsAdapter
    }

    private fun setupServicesGrid() {
        val svcViews = listOf(
            "haircut" to binding.serviceHaircut,
            "shaving" to binding.serviceShaving,
            "combo" to binding.serviceCombo
        )

        svcViews.forEach { (id, view) ->
            view.setOnClickListener {
                // Update the selected service
                selectedService = id

                // Update all views to reflect the current selection
                svcViews.forEach { (svcId, svcView) ->
                    applyServiceSelection(svcView, svcId == selectedService)
                }
            }
        }

        // Initialize default selection (optional)
        selectedService = "haircut"
        svcViews.forEach { (svcId, svcView) ->
            applyServiceSelection(svcView, svcId == selectedService)
        }
    }

    private fun applyServiceSelection(target: View, selected: Boolean) {
        val drawable = if (selected) {
            ContextCompat.getDrawable(
                requireContext(),
                bharatnaai.R.drawable.filter_chip_selected_background
            )
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
        return String.format(
            Locale.US, "%02d / %02d / %04d",
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)
        )
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
            binding.tvNoSlots.visibility =
                if (!hasSlots && !s.isLoading) View.VISIBLE else View.GONE
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
