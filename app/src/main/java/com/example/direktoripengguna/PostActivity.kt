package com.example.direktoripengguna

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.direktoripengguna.data.Post
import com.example.direktoripengguna.data.PostBaru
import com.example.direktoripengguna.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_NAME = "user_name"
    }

    private val repository = UserRepository()
    private val daftarPost = mutableListOf<Post>()
    private var userId = 0

    private lateinit var etJudul: EditText
    private lateinit var btnTambah: Button
    private lateinit var tvStatus: TextView
    private lateinit var listPost: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        title = intent.getStringExtra(EXTRA_USER_NAME) ?: "Postingan"

        etJudul = findViewById(R.id.etJudul)
        btnTambah = findViewById(R.id.btnTambah)
        tvStatus = findViewById(R.id.tvStatus)
        listPost = findViewById(R.id.listPost)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listPost.adapter = adapter

        btnTambah.setOnClickListener { kirimPost() }

        // Pengembangan lanjutan: tekan lama untuk menghapus postingan
        listPost.setOnItemLongClickListener { _, _, position, _ ->
            daftarPost.getOrNull(position)?.let { konfirmasiHapus(it) }
            true
        }

        muatPosts()
    }

    private fun tampilkanPosts() {
        adapter.clear()
        adapter.addAll(daftarPost.map { it.title })
    }

    private fun muatPosts() {
        tvStatus.text = "Memuat postingan..."
        lifecycleScope.launch {
            try {
                val posts = repository.ambilPosts(userId)
                daftarPost.clear()
                daftarPost.addAll(posts)
                tampilkanPosts()
                tvStatus.text = "${posts.size} postingan"
            } catch (e: HttpException) {
                tvStatus.text = "Server error: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }

    private fun kirimPost() {
        val judul = etJudul.text.toString().trim()
        if (judul.isEmpty()) {
            etJudul.error = "Judul tidak boleh kosong"
            return
        }

        btnTambah.isEnabled = false
        tvStatus.text = "Mengirim postingan..."
        lifecycleScope.launch {
            try {
                val baru = PostBaru(
                    title = judul,
                    body = "Dibuat dari aplikasi Direktori Pengguna",
                    userId = userId
                )
                val hasil = repository.kirimPost(baru)
                tvStatus.text = "Tersimpan dengan id ${hasil.id}"
                etJudul.text.clear()
            } catch (e: HttpException) {
                tvStatus.text = "Gagal menyimpan: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            } finally {
                btnTambah.isEnabled = true
            }
        }
    }

    // ---- Pengembangan lanjutan: hapus postingan ----

    private fun konfirmasiHapus(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Hapus postingan?")
            .setMessage(post.title)
            .setPositiveButton("Hapus") { _, _ -> hapusPost(post) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusPost(post: Post) {
        lifecycleScope.launch {
            try {
                val response = repository.hapusPost(post.id)
                if (response.isSuccessful) {
                    // JSONPlaceholder hanya meniru: data di server tidak benar-benar terhapus,
                    // jadi item dihapus dari daftar lokal saja.
                    daftarPost.remove(post)
                    tampilkanPosts()
                    tvStatus.text = "Postingan ${post.id} berhasil dihapus"
                } else {
                    tvStatus.text = "Gagal menghapus: HTTP ${response.code()}"
                }
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
}
