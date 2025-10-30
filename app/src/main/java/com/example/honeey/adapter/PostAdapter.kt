package com.example.honeey.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.honeey.Post // ✅ ini penting — pakai model dari package utama
import com.example.honeey.databinding.ItemPostBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    fun updateData(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        with(holder.binding) {
            Glide.with(imagePost.context)
                .load(post.imageUrl)
                .into(imagePost)
            textDescription.text = post.description
            textLikes.text = "${post.likeCount} suka"

            var lastTapTime = 0L
            imagePost.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastTapTime < 400) { // tap 2x cepat
                    val newCount = post.likeCount + 1
                    textLikes.text = "$newCount suka"

                    // Update ke Firestore
                    FirebaseFirestore.getInstance().collection("posts")
                        .document(post.id)
                        .update("likeCount", newCount)
                        .addOnSuccessListener {
                            // Berhasil diupdate
                        }
                }
                lastTapTime = now
            }
        }
    }

    override fun getItemCount() = posts.size
}
