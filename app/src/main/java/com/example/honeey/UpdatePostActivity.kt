package com.example.honeey

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.honeey.databinding.ActivityUpdatePostBinding
import com.google.firebase.firestore.FirebaseFirestore

class UpdatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePostBinding
    private lateinit var db: FirebaseFirestore
    private var oldImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Ambil data dari intent
        oldImageUrl = intent.getStringExtra("imageUrl")
        val oldDescription = intent.getStringExtra("description")

        // Isi awal di kolom edit
        binding.editImageUrl.setText(oldImageUrl)
        binding.editDescription.setText(oldDescription)

        // Tombol simpan update
        binding.btnUpdate.setOnClickListener {
            val newImageUrl = binding.editImageUrl.text.toString().trim()
            val newDescription = binding.editDescription.text.toString().trim()

            if (newImageUrl.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Isi semua kolom terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update data di Firestore
            db.collection("posts")
                .whereEqualTo("imageUrl", oldImageUrl)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        db.collection("posts").document(doc.id)
                            .update(
                                mapOf(
                                    "imageUrl" to newImageUrl,
                                    "description" to newDescription
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(this, "Postingan berhasil diperbarui ✅", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal memperbarui postingan", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }
}
