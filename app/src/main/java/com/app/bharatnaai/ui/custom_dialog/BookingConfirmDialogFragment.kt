package com.app.bharatnaai.ui.custom_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.app.bharatnaai.ui.my_booking.BookingHistoryFrag
import bharatnaai.R
import bharatnaai.databinding.DialogBookingConfirmBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class BookingConfirmDialogFragment : DialogFragment() {

    private var _binding: DialogBookingConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Bharatnaai)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBookingConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val confirmationNo = arguments?.getString(ARG_CONFIRM_NO) ?: "--"
        val date = arguments?.getString(ARG_DATE) ?: "--"
        val time = arguments?.getString(ARG_TIME) ?: "--"

        binding.tvConfirmNoValue.text = confirmationNo
        binding.tvDateValue.text = date
        binding.tvTimeValue.text = time

        binding.btnDone.setOnClickListener {
            dismissAllowingStateLoss()
            // Navigate to My Bookings (BookingHistoryFrag)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BookingHistoryFrag.newInstance())
                .addToBackStack(null)
                .commit()

            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_bookings
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes.apply { dimAmount = 0.6f }
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_CONFIRM_NO = "confirmNo"
        const val ARG_DATE = "date"
        const val ARG_TIME = "time"

        fun newInstance(confirmNo: String, date: String, time: String): BookingConfirmDialogFragment {
            return BookingConfirmDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONFIRM_NO, confirmNo)
                    putString(ARG_DATE, date)
                    putString(ARG_TIME, time)
                }
            }
        }
    }
}
