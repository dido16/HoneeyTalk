package com.example.honeey

data class Post(
    val id: String = "",
    val imageUrl: String = "",
    val description: String = "",
    var liked: Boolean = false,
    var likeCount: Int = 0
)
