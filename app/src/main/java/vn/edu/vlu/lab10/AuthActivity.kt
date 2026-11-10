package vn.edu.vlu.lab10

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import vn.edu.vlu.lab10.databinding.ActivityAuthBinding

/**
 * AuthActivity: Register / Login screen using Email + Password via Firebase Authentication.
 * - Uses View Binding instead of findViewById().
 * - Logs in automatically if the previous session is still valid.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    // true = Register mode, false = Login mode
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Auto-login: a stored session means we can skip this screen
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupModeToggle()
        setupSubmitButton()
        setupForgotPassword()
    }

    /** Switch Login <-> Register and update the UI accordingly. */
    private fun setupModeToggle() {
        binding.toggleAuthMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener // ignore uncheck events

            isRegisterMode = (checkedId == binding.btnTabRegister.id)

            binding.tilConfirmPassword.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
            binding.tvForgotPassword.visibility = if (isRegisterMode) View.GONE else View.VISIBLE
            binding.btnSubmit.text = getString(
                if (isRegisterMode) R.string.btn_register else R.string.btn_login
            )
            clearErrors()
        }
    }

    /** Validate on the client first, then call Firebase. */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (!validateInput(email, password, confirm)) return@setOnClickListener

            if (isRegisterMode) registerUser(email, password) else loginUser(email, password)
        }
    }

    /** Challenge 1: send a password reset email to the address the user typed. */
    private fun setupForgotPassword() {
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            clearErrors()
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Enter a valid email to receive the password reset link"
                return@setOnClickListener
            }

            showLoading(true)
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Sent a password reset link to $email. Please check your inbox (including Spam).",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    handleAuthError(task.exception)
                }
            }
        }
    }

    /**
     * Returns true if input is valid.
     * Catches: empty/invalid email, password < 6 characters, mismatched confirm password.
     */
    private fun validateInput(email: String, password: String, confirm: String): Boolean {
        clearErrors()
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format (e.g., name@gmail.com)"
            isValid = false
        }

        // Firebase requires at least 6 characters
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (isRegisterMode && password != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }
        return isValid
    }

    /** Register a new account; Firebase signs the new user in automatically. */
    private fun registerUser(email: String, password: String) {
        showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /** Log in with an existing account. */
    private fun loginUser(email: String, password: String) {
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    /** Convert Firebase exceptions into friendly messages. */
    private fun handleAuthError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException ->
                binding.tilPassword.error = "Password is too weak, please use at least 6 characters"

            // Bad email format or wrong credentials
            is FirebaseAuthInvalidCredentialsException ->
                if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                    binding.tilEmail.error = "Invalid email format"
                } else {
                    Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_LONG).show()
                }

            is FirebaseAuthUserCollisionException ->
                binding.tilEmail.error = "This email is already registered, please switch to Login"

            is FirebaseAuthInvalidUserException ->
                Toast.makeText(this, "Account does not exist or has been disabled", Toast.LENGTH_LONG).show()

            is FirebaseNetworkException ->
                Toast.makeText(this, "No network connection, please check again", Toast.LENGTH_LONG).show()

            // Anything else: show the original message to ease debugging
            else ->
                Toast.makeText(this, "Error: ${exception?.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /** Toggle the progress circle and lock the buttons to avoid repeated taps. */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    /** Go to MainActivity and close this one so Back never returns to the login screen. */
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
