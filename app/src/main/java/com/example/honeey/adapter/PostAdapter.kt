package com.example.honeey.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.honeey.databinding.ItemPostBinding
import com.google.firebase.firestore.FirebaseFirestore

// 🔹 Data model postingan
data class Post(
    val imageUrl: String = "",
    val description: String = "",
    var likes: Int = 0,
    val userEmail: String = ""
)

class PostAdapter(
    private val postList: MutableList<Post>,
    private val currentUserEmail: String // email user yang login
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

        // ✅ Perbaiki URL ImgBB agar Glide bisa load
        var fixedUrl = post.imageUrl.trim()
        if (fixedUrl.startsWith("http://")) fixedUrl = fixedUrl.replace("http://", "https://")
        if (fixedUrl.contains("https://ibb.co")) {
            fixedUrl = fixedUrl.replace("https://ibb.co", "https://i.ibb.co")
        } else if (fixedUrl.contains("http://ibb.co")) {
            fixedUrl = fixedUrl.replace("http://ibb.co", "https://i.ibb.co")
        }

        // 🔹 Load gambar dengan Glide
        Glide.with(context)
            .load(fixedUrl)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(holder.binding.imagePost)

        // 🔹 Isi teks deskripsi & email pembuat
        holder.binding.textDescription.text = post.description
        holder.binding.textUserEmail.text =
            "Dipost oleh: ${post.userEmail.ifEmpty { "Tidak diketahui" }}"

        // 🔹 Tampilkan jumlah like
        holder.binding.textLikes.text = "${post.likes} Likes"

        // ❤️ Double-tap untuk like
        holder.binding.imagePost.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastTapTime < 300) {
                post.likes += 1
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

        // 🗑️ Tekan lama untuk hapus postingan (hanya pemilik bisa hapus)
        holder.itemView.setOnLongClickListener {
            if (post.userEmail != currentUserEmail) {
                Toast.makeText(context, "Kamu tidak bisa menghapus postingan orang lain", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }

            AlertDialog.Builder(context)
                .setTitle("Hapus Postingan")
                .setMessage("Yakin mau hapus postingan ini?")
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
                        .addOnFailureListener {
                            Toast.makeText(context, "Gagal menghapus postingan", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Batal", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updateData(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}
