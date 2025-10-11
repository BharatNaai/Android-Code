package com.app.bharatnaai.ui.auth.forgotpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import bharatnaai.R
import bharatnaai.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var binding: ActivityForgotPasswordBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        setupRadioGroup()
        setupTextWatchers()
        observeData()
    }
    
    private fun observeData() {
        viewModel.forgotPasswordState.observe(this) { state ->
            updateUI(state)
        }
    }
    
    private fun updateUI(state: ForgotPasswordState) {
        // Update UI based on current step
        when (state.currentStep) {
            ForgotPasswordStep.SELECT_METHOD -> {
                binding.rgResetOptions.visibility = View.VISIBLE
                binding.llOtpSection.visibility = View.GONE
                binding.btnContinue.text = if (state.isLoading) "Sending..." else "Continue"
            }
            ForgotPasswordStep.VERIFY_OTP -> {
                binding.rgResetOptions.visibility = View.GONE
                binding.llOtpSection.visibility = View.VISIBLE
                binding.btnContinue.text = if (state.isLoading) "Resetting..." else "Reset Password"
                
                // Update OTP subtitle based on selected method
                val methodText = if (state.selectedMethod == ResetMethod.EMAIL) "email" else "SMS"
                binding.tvOtpSubtitle.text = "We have sent a verification code to your $methodText"
            }
            ForgotPasswordStep.RESET_COMPLETE -> {
                // Handle completion - could navigate back or show success message
            }
        }
        
        // Update button state
        binding.btnContinue.isEnabled = !state.isLoading
        
        // Show field errors
        binding.tilOtp.error = state.otpError
        binding.tilNewPassword.error = state.newPasswordError
        binding.tilConfirmPassword.error = state.confirmPasswordError
        
        // Handle success/error states
        when {
            state.isSuccess && state.currentStep == ForgotPasswordStep.RESET_COMPLETE -> {
                Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            state.error != null -> {
                Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnContinue.setOnClickListener {
            val currentState = viewModel.forgotPasswordState.value
            
            when (currentState?.currentStep) {
                ForgotPasswordStep.SELECT_METHOD -> {
                    val selectedMethod = when (binding.rgResetOptions.checkedRadioButtonId) {
                        R.id.rb_sms -> ResetMethod.SMS
                        R.id.rb_email -> ResetMethod.EMAIL
                        else -> ResetMethod.EMAIL
                    }
                    // For demo purposes, using a hardcoded email. In real app, get from user input
                    val email = "user@example.com"
                    viewModel.sendResetCode(selectedMethod, email)
                }
                ForgotPasswordStep.VERIFY_OTP -> {
                    collectFormData()
                    viewModel.resetPassword()
                }
                else -> {
                    // Handle other states if needed
                }
            }
        }
    }
    
    private fun setupRadioGroup() {
        // Set initial selection to EMAIL (as per requirement)
        binding.rbEmail.isChecked = true
        viewModel.setSelectedMethod(ResetMethod.EMAIL)
        
        binding.rgResetOptions.setOnCheckedChangeListener { _, checkedId ->
            val method = when (checkedId) {
                R.id.rb_sms -> ResetMethod.SMS
                R.id.rb_email -> ResetMethod.EMAIL
                else -> ResetMethod.EMAIL
            }
            viewModel.setSelectedMethod(method)
        }
    }
    
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                collectFormData()
                viewModel.validateOtpForm()
            }
        }

        // Add text watchers
        binding.etOtp.addTextChangedListener(textWatcher)
        binding.etNewPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)
        
        // Add focus change listeners to mark fields as touched
        binding.etOtp.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("otp")
                collectFormData()
                viewModel.validateOtpForm()
            }
        }
        
        binding.etNewPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("newPassword")
                collectFormData()
                viewModel.validateOtpForm()
            }
        }
        
        binding.etConfirmPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("confirmPassword")
                collectFormData()
                viewModel.validateOtpForm()
            }
        }
    }
    
    private fun collectFormData() {
        viewModel.updateFormData(
            otp = binding.etOtp.text.toString(),
            newPassword = binding.etNewPassword.text.toString(),
            confirmPassword = binding.etConfirmPassword.text.toString()
        )
    }
}
