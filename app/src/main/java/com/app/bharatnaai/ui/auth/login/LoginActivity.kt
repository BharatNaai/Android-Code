package com.app.bharatnaai.ui.auth.login

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
import com.app.bharatnaai.ui.main.MainActivity
import com.app.bharatnaai.ui.auth.register.RegisterActivity
import bharatnaai.R
import bharatnaai.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var loginbinding: ActivityLoginBinding

    private val viewModel: LoginViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(LoginViewModel::class.java)
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginbinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginbinding.root)

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
        viewModel.loginState.observe(this) { state ->
            updateUI(state)
        }
    }
    
    private fun updateUI(state: LoginState) {
        // Update button state
        loginbinding.btnLogIn.isEnabled = state.isFormValid && !state.isLoading
        loginbinding.btnLogIn.text = if (state.isLoading) "Logging In..." else "Log In"
        
        // Show field errors
        loginbinding.tilEmailPhone.error = state.emailPhoneError
        loginbinding.tilPassword.error = state.passwordError
        
        // Handle success/error states
        when {
            state.isSuccess -> {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            state.error != null -> {
                Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupClickListeners() {
        loginbinding.btnLogIn.setOnClickListener {
            collectFormData()
            viewModel.login()
        }

        loginbinding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, com.app.bharatnaai.ui.auth.forgotpassword.ForgotPasswordActivity::class.java))
        }
        
        // Social login buttons
        loginbinding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        loginbinding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook login - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        loginbinding.btnApple.setOnClickListener {
            Toast.makeText(this, "Apple login - Coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        // Sign up link - both parts clickable
        loginbinding.tvSignUpLink.setOnClickListener {
            navigateToRegister()
        }

        loginbinding.tvSignUpAction.setOnClickListener {
            navigateToRegister()
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
        loginbinding.etEmailPhone.addTextChangedListener(textWatcher)
        loginbinding.etPassword.addTextChangedListener(textWatcher)
        
        // Add focus change listeners to mark fields as touched
        loginbinding.etEmailPhone.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("emailPhone")
                collectFormData()
                viewModel.validateForm()
            }
        }
        
        loginbinding.etPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.markFieldAsTouched("password")
                collectFormData()
                viewModel.validateForm()
            }
        }
    }
    
    private fun collectFormData() {
        viewModel.updateFormData(
            emailPhone = loginbinding.etEmailPhone.text.toString(),
            password = loginbinding.etPassword.text.toString()
        )
    }
    
    private fun navigateToRegister() {
        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
    }
    
    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                    Toast.makeText(this, "Welcome back ${user?.displayName}!", Toast.LENGTH_SHORT).show()
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
}
