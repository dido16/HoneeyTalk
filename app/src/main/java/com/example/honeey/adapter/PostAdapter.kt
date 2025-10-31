package com.example.honeey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.honeey.databinding.ItemPostBinding
import com.google.firebase.firestore.FirebaseFirestore

data class Post(
    val imageUrl: String = "",
    val description: String = "",
    var likes: Int = 0
)

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

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

        holder.binding.textDescription.text = post.description

        // 🔹 Tambahkan placeholder dan error image
        Glide.with(context)
            .load(post.imageUrl)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(holder.binding.imagePost)

        holder.binding.textLikes.text = "${post.likes} Likes"

        // 🔹 Double-tap untuk like
        holder.binding.imagePost.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastTapTime < 300) {
                post.likes += 1
                holder.binding.textLikes.text = "${post.likes} Likes"

                val db = FirebaseFirestore.getInstance()
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
    }

    override fun getItemCount(): Int = postList.size

    fun updateData(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}
