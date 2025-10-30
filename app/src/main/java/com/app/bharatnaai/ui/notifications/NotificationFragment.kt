package com.app.bharatnaai.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bharatnaai.databinding.FragmentNotificationsBinding
import com.app.bharatnaai.data.model.NotificationItem
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    companion object {
        fun newInstance() = NotificationFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        observeViewModel()

        val initialTop = binding.headerContainer.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = initialTop + systemBars.top)
            insets
        }
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            onNotificationClick(notification)
        }
        
        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        viewModel.notificationSections.observe(viewLifecycleOwner) { sections ->
            notificationAdapter.submitList(sections)
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            binding.emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvNotifications.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add loading indicator here if needed
            // For now, we'll keep it simple
        }
    }

    private fun onNotificationClick(notification: NotificationItem) {
        // Mark notification as read
        viewModel.markNotificationAsRead(notification.id)
        
        // Handle navigation based on notification type or actionData
        // For now, we'll just show a simple response
        // You can extend this to navigate to specific screens
        when (notification.type) {
            com.app.bharatnaai.data.model.NotificationType.APPOINTMENT_CONFIRMED,
            com.app.bharatnaai.data.model.NotificationType.APPOINTMENT_REMINDER,
            com.app.bharatnaai.data.model.NotificationType.APPOINTMENT_UPDATED -> {
                // Navigate to booking details or calendar
                // For now, just a placeholder
            }
            com.app.bharatnaai.data.model.NotificationType.SPECIAL_OFFER -> {
                // Navigate to offers or salon details
                // For now, just a placeholder
            }
            com.app.bharatnaai.data.model.NotificationType.GENERAL -> {
                // Handle general notifications
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
