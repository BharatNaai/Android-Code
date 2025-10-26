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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {

    private val viewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var storedVerificationId: String? = null
    private lateinit var verificationCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        initPhoneVerificationCallbacks()

        setupClickListeners()
        setupRadioGroup()
        setupTextWatchers()
        observeData()
    }

    private fun observeData() {
        viewModel.forgotPasswordState.observe(this) { state ->
            updateUI(state)
        }

        // Resend OTP click
        binding.tvResend.setOnClickListener {
            val phone = viewModel.forgotPasswordState.value?.phone
            val normalized =
                phone ?: normalizePhoneNumber(binding.etPhone.text?.toString()?.trim().orEmpty())
            if (normalized.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startPhoneNumberVerification(normalized, resendToken)
        }
    }

    private fun updateUI(state: ForgotPasswordState) {
        // Update UI based on current step
        when (state.currentStep) {
            ForgotPasswordStep.SELECT_METHOD -> {
                binding.rgResetOptions.visibility = View.VISIBLE
                binding.llOtpSection.visibility = View.GONE
                // Toggle input containers based on selection
                if (state.selectedMethod == ResetMethod.EMAIL) {
                    binding.tilEmail.visibility = View.VISIBLE
                    binding.tilPhone.visibility = View.GONE
                    binding.btnContinue.text =
                        if (state.isLoading) "Sending..." else "Send Reset Email"
                } else {
                    binding.tilEmail.visibility = View.GONE
                    binding.tilPhone.visibility = View.VISIBLE
                    binding.btnContinue.text = if (state.isLoading) "Sending..." else "Send OTP"
                }
            }

            ForgotPasswordStep.VERIFY_OTP -> {
                binding.rgResetOptions.visibility = View.GONE
                binding.llOtpSection.visibility = View.VISIBLE
                binding.btnContinue.text = if (state.isLoading) "Verifying..." else "Reset Password"

                // Update OTP subtitle based on selected method
                val methodText = if (state.selectedMethod == ResetMethod.EMAIL) "email" else "SMS"
                binding.tvOtpSubtitle.text = "We have sent a verification code to your $methodText"
                // Show Resend OTP only for SMS method
                binding.tvResend.visibility =
                    if (state.selectedMethod == ResetMethod.SMS) View.VISIBLE else View.GONE
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
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
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
                    if (selectedMethod == ResetMethod.EMAIL) {
                        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
                        if (email.isEmpty()) {
                            binding.tilEmail.error = "Please enter email"
                            return@setOnClickListener
                        } else {
                            binding.tilEmail.error = null
                        }
                        viewModel.setSelectedMethod(ResetMethod.EMAIL)
                        viewModel.setContact(email = email)
                        viewModel.sendResetCode(ResetMethod.EMAIL, email)
                    } else {
                        val phoneRaw = binding.etPhone.text?.toString()?.trim().orEmpty()
                        val phone = normalizePhoneNumber(phoneRaw)
                        if (phone == null) {
                            binding.tilPhone.error = "Please enter phone number"
                            return@setOnClickListener
                        } else {
                            binding.tilPhone.error = null
                        }
                        viewModel.setSelectedMethod(ResetMethod.SMS)
                        viewModel.setContact(phone = phone)
                        startPhoneNumberVerification(phone)
                    }
                }

                ForgotPasswordStep.VERIFY_OTP -> {
                    collectFormData()
                    val method = viewModel.forgotPasswordState.value?.selectedMethod
                    if (method == ResetMethod.SMS) {
                        val code = binding.etOtp.text?.toString()?.trim().orEmpty()
                        if (code.length != 6 || storedVerificationId.isNullOrEmpty()) {
                            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        val credential =
                            PhoneAuthProvider.getCredential(storedVerificationId!!, code)
                        signInWithPhoneAuthCredential(credential)
                    } else {
                        viewModel.resetPassword()
                    }
                }

                else -> {
                    // Handle other states if needed
                }
            }
        }
    }

    private fun setupRadioGroup() {
        // Default to Email
        binding.rbEmail.isChecked = true
        viewModel.setSelectedMethod(ResetMethod.EMAIL)
        // Reflect initial visibility
        binding.tilEmail.visibility = View.VISIBLE
        binding.tilPhone.visibility = View.GONE
        binding.btnContinue.text = "Send Reset Email"

        binding.rgResetOptions.setOnCheckedChangeListener { _, checkedId ->
            val method = when (checkedId) {
                R.id.rb_sms -> ResetMethod.SMS
                R.id.rb_email -> ResetMethod.EMAIL
                else -> ResetMethod.EMAIL
            }
            viewModel.setSelectedMethod(method)
            if (method == ResetMethod.EMAIL) {
                binding.tilEmail.visibility = View.VISIBLE
                binding.tilPhone.visibility = View.GONE
                binding.btnContinue.text = "Send Reset Email"
            } else {
                binding.tilEmail.visibility = View.GONE
                binding.tilPhone.visibility = View.VISIBLE
                binding.btnContinue.text = "Send OTP"
            }
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

        binding.etConfirmPassword.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
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

    private fun startPhoneNumberVerification(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        binding.btnContinue.isEnabled = false
        val builder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(verificationCallbacks)
        if (token != null) builder.setForceResendingToken(token)
        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    private fun initPhoneVerificationCallbacks() {
        verificationCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant validation
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.btnContinue.isEnabled = true
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    e.localizedMessage ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.btnContinue.isEnabled = true
                storedVerificationId = verificationId
                resendToken = token
                viewModel.onCodeSent(verificationId)
                viewModel.goToVerifyOtpStep(ResetMethod.SMS)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        binding.btnContinue.isEnabled = false
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.btnContinue.isEnabled = true
                if (task.isSuccessful) {
                    // Phone verified; in a real flow you might now allow password reset on server
                    Toast.makeText(this, "Phone verified", Toast.LENGTH_SHORT).show()
                    // Move to password reset step if your backend supports phone-based reset; otherwise finish.
                    viewModel.goToVerifyOtpStep(ResetMethod.SMS)
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Invalid OTP",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun normalizePhoneNumber(input: String): String? {
        if (input.isEmpty()) return null
        val trimmed = input.replace("\\s+".toRegex(), "")
        // Basic validation: must start with '+' and have 10+ digits
        return if (trimmed.startsWith("+") && trimmed.drop(1)
                .all { it.isDigit() } && trimmed.length >= 11
        ) {
            trimmed
        } else null
    }
}
