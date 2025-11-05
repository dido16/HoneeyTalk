package com.example.honeey

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.honeey.databinding.ActivityCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.btnUploadPost.setOnClickListener {
            uploadPost()
        }
    }

    private fun uploadPost() {
        val desc = binding.inputDescription.text.toString().trim()
        val imageUrl = binding.inputImageUrl.text.toString().trim() // 🔹 ambil link gambar dari EditText

        if (desc.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Isi deskripsi dan link gambar!", Toast.LENGTH_SHORT).show()
            return
        }

        savePostToFirestore(imageUrl, desc)
    }

    private fun savePostToFirestore(imageUrl: String, desc: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "anonymous"

        val post = hashMapOf(
            "imageUrl" to imageUrl,
            "description" to desc,
            "likes" to 0,
            "userEmail" to userEmail
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Postingan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
