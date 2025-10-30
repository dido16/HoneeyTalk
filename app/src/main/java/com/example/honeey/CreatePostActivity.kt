package com.example.honeey

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.honeey.databinding.ActivityCreatePostBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.btnSelectImage.setOnClickListener { selectImage() }
        binding.btnUploadPost.setOnClickListener { uploadPost() }
    }

    // Pilih gambar dari galeri
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imagePreview.setImageURI(imageUri)
        }
    }

    // Upload gambar ke Firebase Storage dan simpan data ke Firestore
    private fun uploadPost() {
        val desc = binding.inputDescription.text.toString().trim()

        if (imageUri == null || desc.isEmpty()) {
            Toast.makeText(this, "Lengkapi gambar dan deskripsi!", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().reference.child("posts/$filename.jpg")

        // 🔹 Upload file ke Firebase Storage
        val uploadTask = storageRef.putFile(imageUri!!)
        uploadTask
            .addOnSuccessListener {
                // 🔹 Setelah upload berhasil, ambil URL file
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        savePostToFirestore(uri.toString(), desc)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal ambil URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Simpan data postingan ke Firestore
    private fun savePostToFirestore(imageUrl: String, desc: String) {
        val post = hashMapOf(
            "imageUrl" to imageUrl,
            "description" to desc,
            "likes" to 0
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Postingan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
