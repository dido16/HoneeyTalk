package com.example.honeey

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.honeey.adapter.PostAdapter
import com.example.honeey.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupEvents()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(mutableListOf("Postingan pertama 🍯", "Selamat datang di Honeey! 🐝"))
        binding.recyclerPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerPosts.adapter = adapter
    }

    private fun setupEvents() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil logout 🐝", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnAddPost.setOnClickListener {
//            val intent = Intent(this, CreatePostActivity::class.java)
//            startActivity(intent)
        }
    }
}
