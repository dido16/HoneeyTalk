package com.example.honeey

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.honeey.databinding.ActivityCreatePostBinding
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    // 🔹 API Key ImgBB kamu
    private val imgbbApiKey = "9973f8d92d8b3b35c8ad4a6baf9ed757"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // 🔹 Minta izin akses gambar
        requestStoragePermission()

        binding.btnSelectImage.setOnClickListener { selectImage() }
        binding.btnUploadPost.setOnClickListener { uploadPost() }
    }

    // 🔹 Minta izin runtime (Android 13 ke atas)
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    // 🔹 Pilih gambar dari galeri
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    // 🔹 Tampilkan gambar yang dipilih
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.imagePreview.setImageURI(imageUri)
        }
    }

    // 🔹 Upload ke ImgBB dan simpan ke Firestore
    private fun uploadPost() {
        val desc = binding.inputDescription.text.toString().trim()

        if (imageUri == null || desc.isEmpty()) {
            Toast.makeText(this, "Lengkapi gambar dan deskripsi!", Toast.LENGTH_SHORT).show()
            return
        }

        val inputStream = contentResolver.openInputStream(imageUri!!)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes == null) {
            Toast.makeText(this, "Gagal membaca gambar!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "upload.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
            )
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$imgbbApiKey")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@CreatePostActivity,
                        "Upload gagal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    // 🔹 Ambil dan bersihkan URL ImgBB
                    val imageUrl = Regex("\"display_url\":\"(.*?)\"")
                        .find(responseBody)?.groups?.get(1)?.value
                        ?.replace("\\/", "/")

                    runOnUiThread {
                        if (imageUrl != null) {
                            savePostToFirestore(imageUrl, desc)
                        } else {
                            Toast.makeText(
                                this@CreatePostActivity,
                                "Gagal ambil URL gambar!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@CreatePostActivity,
                            "Gagal upload ke ImgBB!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // 🔹 Simpan data ke Firestore dan langsung balik ke HomeActivity
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

                // 🔹 Setelah sukses, langsung kembali ke HomeActivity
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
