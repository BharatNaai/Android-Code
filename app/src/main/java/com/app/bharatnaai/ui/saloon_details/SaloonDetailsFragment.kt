package com.app.bharatnaai.ui.saloon_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bharatnaai.R
import bharatnaai.databinding.FragmentSaloonDetailsBinding
import com.app.bharatnaai.data.model.Barber
import com.app.bharatnaai.ui.barber_details.BarberDetailsFragment
import com.app.bharatnaai.utils.CommonMethod

class SaloonDetailsFragment : Fragment() {
    private var _binding: FragmentSaloonDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SaloonDetailsViewModel by viewModels()
    private lateinit var barbersAdapter: OurBarbersAdapter
    private val commonMethod = CommonMethod()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaloonDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        setupRecycler()
        observeViewModel()

        val salonId = arguments?.getInt("salonId")
        if (salonId != null) {
            viewModel.loadSalon(salonId)
        }
    }

    private fun setupRecycler() {
        barbersAdapter = OurBarbersAdapter(
            onBarberClick = { barber -> navigateToBarberDetails(barber) },
            onBookNow = { barber -> navigateToBarberDetails(barber) }
        )
        binding.rvBarbers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBarbers.adapter = barbersAdapter
    }

    private fun navigateToBarberDetails(barber: Barber) {
        val fragment = BarberDetailsFragment()
        fragment.arguments = Bundle().apply {
            putInt("barberId", barber.barberId)
            putString("barberName", barber.barberName)
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { s ->
            binding.swipeRefresh.isRefreshing = s.isLoading
            binding.tvTitle.text = s.salonName
            binding.tvAboutDesc.text = s.about
            binding.tvAddress.text = s.address
            binding.tvHours.text = s.hours

            commonMethod.loadImage(binding.ivSalon, s.saloonImage)

            if (s.error != null) {
                Toast.makeText(requireContext(), s.error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.barbers.observe(viewLifecycleOwner) { list ->
            barbersAdapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SaloonDetailsFragment()
    }
}
