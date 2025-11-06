package com.example.honeey

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.honeey.adapter.Post
import com.example.honeey.adapter.PostAdapter
import com.example.honeey.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: PostAdapter
    private lateinit var firestore: FirebaseFirestore
    private var listener: ListenerRegistration? = null
    private lateinit var currentUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        adapter = PostAdapter(mutableListOf(), currentUserEmail)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPosts.adapter = adapter

        //  Tombol tambah postingan
        binding.btnAddPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        //  Tombol Logout
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // keluar dari akun
            Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()

            // Kembali ke halaman login
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        //  Realtime listener Firestore (update otomatis)
        listener = firestore.collection("posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                    adapter.updateData(posts.reversed()) // postingan terbaru di atas
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove() // hentikan listener saat activity ditutup
    }
}
