package com.example.direktoripengguna.repository

import com.example.direktoripengguna.data.Post
import com.example.direktoripengguna.data.PostBaru
import com.example.direktoripengguna.data.User
import com.example.direktoripengguna.network.RetrofitClient
import retrofit2.Response

// Pengembangan lanjutan: membungkus RetrofitClient.api agar Activity lebih ringkas
class UserRepository {

    suspend fun ambilUsers(): List<User> = RetrofitClient.api.getUsers()

    suspend fun ambilPosts(userId: Int): List<Post> = RetrofitClient.api.getPostsByUser(userId)

    suspend fun kirimPost(post: PostBaru): Post = RetrofitClient.api.buatPost(post)

    suspend fun hapusPost(id: Int): Response<Unit> = RetrofitClient.api.hapusPost(id)
}
