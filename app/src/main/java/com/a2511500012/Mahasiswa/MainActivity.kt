
package com.a2511500012.Mahasiswa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.a2511500012.Mahasiswa.adapter.MahasiswaAdapter
import com.a2511500012.Mahasiswa.api.ApiConfig
import com.a2511500012.Mahasiswa.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MahasiswaAdapter

    // buka form, refresh jika form mengembalikan RESULT_OK
    private val tambahLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadData()
        }
    }

    // onCreate, setupRecyclerView, loadData, showDetail: sama seperti P10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupActions()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = MahasiswaAdapter { mahasiswa ->
            showDetail(mahasiswa.id)
        }

        binding.rvMahasiswa.layoutManager = LinearLayoutManager(this)
        binding.rvMahasiswa.adapter = adapter
    }

    private fun setupActions() {
        // ganti dari Toast ke pindah activity dengan intent
        binding.btnTambah.setOnClickListener {
            tambahLauncher.launch(
                Intent(this, TambahMahasiswaActivity::class.java)
            )
        }

        binding.btnCari.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            loadData(keyword.ifEmpty { null })
        }

        binding.btnReset.setOnClickListener {
            binding.etSearch.text?.clear()
            loadData()
        }
    }

    private fun loadData(keyword: String? = null) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (keyword.isNullOrEmpty()) {
                        ApiConfig.getApiService().getDaftarMahasiswa()
                    } else {
                        ApiConfig.getApiService().searchMahasiswa(keyword)
                    }
                }

                if (response.isSuccessful) {
                    val list = response.body()?.data.orEmpty()
                    adapter.updateData(list)
                    binding.tvHeader.text = "Data Mahasiswa (${list.size})"

                    if (list.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text =
                            if (keyword.isNullOrEmpty()) "Belum ada data"
                            else "Tidak ditemukan untuk \"$keyword\""
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Gagal memuat (HTTP ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text =
                        "Gagal memuat (HTTP ${response.code()})"
                }
            } catch (e: SocketTimeoutException) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text =
                    "Timeout — periksa IP & port server."
            } catch (e: IOException) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text =
                    "Gagal terhubung (${e.localizedMessage})"
            } catch (e: HttpException) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text =
                    "Kesalahan HTTP (${e.code()})"
            } catch (e: Exception) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text =
                    "Terjadi kesalahan (${e.localizedMessage})"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // GET by ID: dipanggil saat item diklik.
    // Di P12 id ini dipakai untuk PUT/DELETE.
    private fun showDetail(id: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getDetailMahasiswa(id)
                }

                if (response.isSuccessful) {
                    val m = response.body()?.data

                    if (m != null) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(m.nama)
                            .setMessage(
                                "ID: ${m.id}\nNIM: ${m.nim}\nJurusan: ${m.jurusan}"
                            )
                            .setPositiveButton("Tutup", null)
                            .show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Data tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Detail gagal (HTTP ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Gagal memuat detail (${e.localizedMessage})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
