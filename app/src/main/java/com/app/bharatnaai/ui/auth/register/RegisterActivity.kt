package com.app.bharatnaai.ui.auth.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.bharatnaai.MainActivity
import com.app.bharatnaai.ui.auth.login.LoginActivity
import bharatnaai.R
import bharatnaai.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "RegisterActivity"
    }
    
    private val viewModel: RegisterViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(RegisterViewModel::class.java)
    }
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    
    // Modern Activity Result API
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In result: ${result.resultCode}")
        
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        Log.d(TAG, "Google Sign-In successful for: ${account.email}")
                        val idToken = account.idToken
                        if (idToken != null) {
                            firebaseAuthWithGoogle(idToken)
                        } else {
                            Log.e(TAG, "ID Token is null")
                            Toast.makeText(this, "Failed to get authentication token", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Google account is null")
                        Toast.makeText(this, "Failed to get Google account", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign-in failed with code: ${e.statusCode}", e)
                    Toast.makeText(this, "Google sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Google Sign-In was canceled by user")
                Toast.makeText(this, "Sign-in canceled", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e(TAG, "Google Sign-In failed with result code: ${result.resultCode}")
                Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupClickListeners()
        setupTextWatchers()
        observeData()
    }
    
    private fun initializeFirebase() {
        // Check if Google Play Services is available
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services is not available", Toast.LENGTH_LONG).show()
            return
        }

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val webClientId = getString(R.string.default_web_client_id)
        Log.d(TAG, "Using web client ID: ${webClientId.take(20)}...")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d(TAG, "Firebase and Google Sign-In initialized successfully")
        
        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: ${currentUser.email}")
        }
    }
    
    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    private fun observeData() {
        viewModel.registerState.observe(this) { state ->
            updateUI(state)
        }
    }
    
    private fun updateUI(state: RegisterState) {
        // Update button state
        binding.btnSignUp.isEnabled = state.isFormValid && !state.isLoading
        binding.btnSignUp.text = if (state.isLoading) "Loading..." else "Sign Up"
        
        // Show field errors
        binding.tilFullName.error = state.fullNameError
        binding.tilEmail.error = state.emailError
        binding.tilPhone.error = state.phoneError
        binding.tilPassword.error = state.passwordError
        binding.tilConfirmPassword.error = state.confirmPasswordError
        
        // Handle success/error states
        when {
            state.isSuccess -> {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                navigateToLogin()
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
        
        binding.btnSignUp.setOnClickListener {
            collectFormData()
            viewModel.register()
        }
        
        // Social login buttons
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook sign up - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnInstagram.setOnClickListener {
            Toast.makeText(this, "Instagram sign up - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        binding.tvSignInLink.setOnClickListener {
            navigateToLogin()
        }

        binding.tvSignInAction.setOnClickListener {
            navigateToLogin()
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
        binding.etConfirmPassword.addTextChangedListener(textWatcher)
        
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
        
        binding.etConfirmPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("confirmPassword")
                collectFormData()
                viewModel.validateForm()
            }
        }
    }
    
    private fun collectFormData() {
        viewModel.updateFormData(
            fullName = binding.etFullName.text.toString(),
            email = binding.etEmail.text.toString(),
            phone = binding.etPhone.text.toString(),
            password = binding.etPassword.text.toString(),
            confirmPassword = binding.etConfirmPassword.text.toString()
        )
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In")
        
        // Check if Google Play Services is available before proceeding
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services is not available", Toast.LENGTH_LONG).show()
            return
        }
        
        try {
            // Check if there's already a signed-in account
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
            Log.d(TAG, "Last signed in account: ${lastSignedInAccount?.email}")
            
            // Try direct sign-in first (simpler approach)
            val signInIntent = googleSignInClient.signInIntent
            Log.d(TAG, "Launching Google Sign-In intent directly")
            googleSignInLauncher.launch(signInIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Google Sign-In", e)
            Toast.makeText(this, "Failed to start Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "Starting Firebase authentication with Google")
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "Firebase authentication successful for: ${user?.email}")
                    Toast.makeText(this, "Welcome ${user?.displayName}!", Toast.LENGTH_SHORT).show()
                    
                    // TODO: Send user data to your backend and save tokens
                    // For now, navigate to MainActivity
                    navigateToMain()
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Firebase authentication failed", exception)
                    Toast.makeText(this, "Authentication failed: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Firebase authentication failed with exception", exception)
                Toast.makeText(this, "Authentication failed: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
