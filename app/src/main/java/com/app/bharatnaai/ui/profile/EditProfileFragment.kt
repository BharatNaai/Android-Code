package com.app.bharatnaai.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import bharatnaai.databinding.FragmentEditProfileBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                updateProfileImage(uri)
                viewModel.updateProfileImage(uri.toString())
            }
        }
    }

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupTextWatchers()
        observeViewModel()
        
        // Load user profile data
        viewModel.loadUserProfile(requireContext())
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.cameraIconContainer.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                collectFormData()
                viewModel.validateForm()
            }
        }
        
        // Add text watchers
        binding.etFullName.addTextChangedListener(textWatcher)
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPhone.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
        
        // Add focus change listeners to mark fields as touched
        binding.etFullName.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("fullName")
                collectFormData()
                viewModel.validateForm()
            }
        }
        
        binding.etEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("email")
                collectFormData()
                viewModel.validateForm()
            }
        }
        
        binding.etPhone.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("phone")
                collectFormData()
                viewModel.validateForm()
            }
        }
        
        binding.etPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("password")
                collectFormData()
                viewModel.validateForm()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.etFullName.setText(profile.name)
            binding.etEmail.setText(profile.email)
            binding.etPhone.setText(profile.phone)
            
            // Load profile image if available
            profile.profileImageUrl?.let { imageUrl ->
                loadProfileImage(imageUrl)
            }
        }

        viewModel.editProfileState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }

        viewModel.profileImageUri.observe(viewLifecycleOwner) { imageUri ->
            imageUri?.let {
                updateProfileImage(Uri.parse(it))
            }
        }
    }
    
    private fun updateUI(state: EditProfileState) {
        // Update button state
        binding.btnSaveChanges.isEnabled = state.isFormValid && !state.isLoading
        binding.btnSaveChanges.text = if (state.isLoading) "Saving..." else "Save Changes"
        
        // Show field errors
        binding.tilFullName.error = state.nameError
        binding.tilEmail.error = state.emailError
        binding.tilPhone.error = state.phoneError
        binding.tilPassword.error = state.passwordError
        
        // Handle success/error states
        when {
            state.isSuccess -> {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
                parentFragmentManager.popBackStack()
            }
            state.error != null -> {
                Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take Photo")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .transform(CircleCrop())
            .into(binding.ivProfileImage)
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .transform(CircleCrop())
            .into(binding.ivProfileImage)
    }

    private fun collectFormData() {
        viewModel.updateFormData(
            fullName = binding.etFullName.text.toString(),
            email = binding.etEmail.text.toString(),
            phone = binding.etPhone.text.toString(),
            password = binding.etPassword.text.toString()
        )
    }
    
    private fun saveProfile() {
        collectFormData()
        viewModel.saveProfile(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
