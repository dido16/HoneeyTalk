package com.example.honeey

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.honeey.adapter.PostAdapter
import com.example.honeey.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PostAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView dan Event
        setupRecyclerView()
        setupEvents()

        // Ambil data postingan dari Firestore
        loadPostsFromFirestore()
    }

    // --- Setup RecyclerView ---
    private fun setupRecyclerView() {
        adapter = PostAdapter(mutableListOf())
        binding.recyclerPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerPosts.adapter = adapter
    }

    // --- Ambil data dari Firestore ---
    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Gagal memuat postingan 🐝", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val posts = value?.toObjects(Post::class.java) ?: emptyList()
                adapter.updateData(posts)
            }
    }

    // --- Event Listener ---
    private fun setupEvents() {
        // Tombol Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil logout 🐝", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Tombol Tambah Postingan
        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
    }
}
