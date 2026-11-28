package com.a2511500012.Mahasiswa

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.a2511500012.Mahasiswa.api.ApiConfig
import com.a2511500012.Mahasiswa.databinding.ActivityTambahMahasiswaBinding
import com.a2511500012.Mahasiswa.model.Jurusan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class TambahMahasiswaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahMahasiswaBinding

    private var daftarJurusan = listOf<Jurusan>()

    private fun parseErrorMessage(response: Response<*>): String {
        return try {
            val raw = response.errorBody()?.string()
            if (raw.isNullOrEmpty()) return response.message()
            JSONObject(raw).optString("message", response.message())
        } catch (e: Exception) {
            response.message()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahMahasiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tambah Mahasiswa"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadJurusan()

        binding.btnSimpan.setOnClickListener {
            simpan()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadJurusan() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getDaftarJurusan()
                }

                if (response.isSuccessful && response.body()?.status == true) {
                    daftarJurusan = response.body()?.data ?: emptyList()

                    val namaJurusan = daftarJurusan.map {
                        it.namaJurusan
                    }

                    val adapter = ArrayAdapter(
                        this@TambahMahasiswaActivity,
                        android.R.layout.simple_spinner_item,
                        namaJurusan
                    )

                    adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )

                    binding.spinnerJurusan.adapter = adapter

                } else {
                    Toast.makeText(
                        this@TambahMahasiswaActivity,
                        "Gagal memuat jurusan: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@TambahMahasiswaActivity,
                    "Gagal memuat jurusan (${e.localizedMessage})",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun simpan() {
        val nama = binding.etNama.text.toString().trim()
        val nim = binding.etNim.text.toString().trim()

        // 1. Validasi client-side (sebelum request)
        if (nama.isEmpty() || nim.isEmpty() || daftarJurusan.isEmpty()) {
            Toast.makeText(
                this,
                "Lengkapi nama, NIM, dan jurusan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (nim.length < 8) {
            binding.etNim.error = "NIM minimal 8 karakter"
            return
        }

        val jurusanId = daftarJurusan[binding.spinnerJurusan.selectedItemPosition].id

        // Kunci tombol agar tidak double-click
        binding.btnSimpan.isEnabled = false
        binding.btnSimpan.text = "Menyimpan..."
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val body = mapOf(
                    "nama" to nama,
                    "nim" to nim,
                    "jurusan_id" to jurusanId.toString()
                )

                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().addMahasiswa(body)
                }

                // Kalau sukses
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(
                        this@TambahMahasiswaActivity,
                        "Tersimpan",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK) // pemicu refresh di MainActivity
                    finish()
                } else {
                    // Gagal, kembalikan code dan pesan dari server
                    val code = response.code()
                    val msg = if (response.body() != null) {
                        response.body()?.message ?: response.message()
                    } else {
                        parseErrorMessage(response)
                    }

                    Toast.makeText(
                        this@TambahMahasiswaActivity,
                        "Gagal ($code): $msg",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: SocketTimeoutException) {
                Toast.makeText(
                    this@TambahMahasiswaActivity,
                    "Timeout — periksa IP & port server",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@TambahMahasiswaActivity,
                    "Gagal terhubung (${e.localizedMessage})",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: HttpException) {
                Toast.makeText(
                    this@TambahMahasiswaActivity,
                    "Kesalahan HTTP (${e.code()})",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@TambahMahasiswaActivity,
                    "Terjadi kesalahan (${e.localizedMessage})",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnSimpan.isEnabled = true
                binding.btnSimpan.text = "SIMPAN"
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}