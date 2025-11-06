package com.example.honeey.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.honeey.UpdatePostActivity
import com.example.honeey.databinding.ItemPostBinding
import com.google.firebase.firestore.FirebaseFirestore

data class Post(
    val imageUrl: String = "",
    val description: String = "",
    var likes: Int = 0,
    val userEmail: String = ""
)

class PostAdapter(
    private val postList: MutableList<Post>,
    private val currentUserEmail: String
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var lastTapTime = 0L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val context = holder.itemView.context
        val db = FirebaseFirestore.getInstance()

        //  Pastikan URL valid
        var fixedUrl = post.imageUrl.trim()
        if (fixedUrl.startsWith("http://")) fixedUrl = fixedUrl.replace("http://", "https://")
        if (fixedUrl.contains("https://ibb.co")) {
            fixedUrl = fixedUrl.replace("https://ibb.co", "https://i.ibb.co")
        }

        //  Tampilkan gambar
        Glide.with(context)
            .load(fixedUrl)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(holder.binding.imagePost)

        //  Isi teks
        holder.binding.textDescription.text = post.description
        holder.binding.textUserEmail.text =
            "Dipost oleh: ${post.userEmail.ifEmpty { "Tidak diketahui" }}"
        holder.binding.textLikes.text = "${post.likes} Likes"

        //  Double tap untuk like
        holder.binding.imagePost.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastTapTime < 300) {
                post.likes++
                holder.binding.textLikes.text = "${post.likes} Likes"
                db.collection("posts")
                    .whereEqualTo("imageUrl", post.imageUrl)
                    .get()
                    .addOnSuccessListener { docs ->
                        for (doc in docs) {
                            db.collection("posts").document(doc.id).update("likes", post.likes)
                        }
                    }
                Toast.makeText(context, "❤️ Kamu menyukai postingan ini", Toast.LENGTH_SHORT).show()
            }
            holder.lastTapTime = currentTime
        }

        //  Tombol titik tiga
        holder.binding.btnMore.setOnClickListener {
            if (post.userEmail != currentUserEmail) {
                Toast.makeText(context, "Kamu tidak bisa mengedit postingan orang lain", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = arrayOf("Edit Postingan", "Hapus Postingan", "Batal")
            AlertDialog.Builder(context)
                .setTitle("Pilih Aksi")
                .setItems(options) { dialog, which ->
                    when (which) {
                        // Edit
                        0 -> {
                            val intent = Intent(context, UpdatePostActivity::class.java)
                            intent.putExtra("imageUrl", post.imageUrl)
                            intent.putExtra("description", post.description)
                            context.startActivity(intent)
                        }
                        // Hapus
                        1 -> {
                            AlertDialog.Builder(context)
                                .setTitle("Konfirmasi Hapus")
                                .setMessage("Yakin ingin menghapus postingan ini?")
                                .setPositiveButton("Ya") { _, _ ->
                                    db.collection("posts")
                                        .whereEqualTo("imageUrl", post.imageUrl)
                                        .get()
                                        .addOnSuccessListener { docs ->
                                            for (doc in docs) {
                                                db.collection("posts").document(doc.id).delete()
                                            }
                                            Toast.makeText(context, "Postingan dihapus", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                        }
                        else -> dialog.dismiss()
                    }
                }
                .show()
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updateData(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}
