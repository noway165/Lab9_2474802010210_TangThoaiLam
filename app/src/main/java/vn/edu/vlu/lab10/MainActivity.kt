package vn.edu.vlu.lab10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import vn.edu.vlu.lab10.databinding.ActivityMainBinding

/** MainActivity: home page with the Glide image list, a circular avatar and a Logout button. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Guard: no session (expired / logged out) -> back to the Auth screen
        val user = auth.currentUser
        if (user == null) {
            navigateToAuth()
            return
        }

        binding.tvWelcome.text = "Hello, ${user.email}"

        setupRecyclerView()
        setupLogoutButton()
        loadAvatar(user.email)
    }

    /** Vertical list with the 10 mock photos. */
    private fun setupRecyclerView() {
        binding.rvPhotos.layoutManager = LinearLayoutManager(this)
        binding.rvPhotos.adapter = PhotoAdapter(PhotoDataSource.getPhotos())
        binding.rvPhotos.setHasFixedSize(true) // list size does not depend on content
    }

    /** Sign out, then return to AuthActivity with a cleared back stack. */
    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            navigateToAuth()
        }
    }

    /** Challenge 2: load a per-account avatar and crop it to a circle with circleCrop(). */
    private fun loadAvatar(email: String?) {
        val seed = email ?: "guest" // stable, unique avatar per account
        val avatarUrl = "https://i.pravatar.cc/200?u=$seed"

        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_broken_image)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivAvatar)
    }

    /** Clear the whole back stack so Back cannot return to the home page. */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
