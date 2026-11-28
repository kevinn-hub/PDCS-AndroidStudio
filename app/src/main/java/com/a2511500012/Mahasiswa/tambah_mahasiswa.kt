
package com.a2511500012.Mahasiswa

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.a2511500012.Mahasiswa.api.ApiConfig
import com.a2511500012.Mahasiswa.databinding.ActivityTambahMahasiswaBinding
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

    private fun parseErrorMessage(response: Response<*>): String {
         return try {
             val raw = response.errorBody()?.string()
             if (raw.isNullOrEmpty()) return response.message()
             JSONObject(raw).optString("message", response.message())
             } catch (_: Exception) {
             response.message()
             }
         }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahMahasiswaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Tambah Mahasiswa"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSimpan.setOnClickListener {
            simpan()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun simpan() {
        val nama = binding.etNama.text.toString().trim()
        val nim = binding.etNim.text.toString().trim()
        val jurusanId = binding.etJurusanId.text.toString().trim()

        // 1. Validasi client-side (sebelum request)
        if (nama.isEmpty() || nim.isEmpty() || jurusanId.isEmpty()) {
            Toast.makeText(
                this,
                "Lengkapi nama, NIM, dan Jurusan ID",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (nim.length < 8) {
            binding.etNim.error = "NIM minimal 8 karakter"
            return
        }

        if (jurusanId.toIntOrNull() == null) {
            binding.etJurusanId.error = "Jurusan ID harus angka (cek tabel jurusan)"
            return
        }

        // Kunci tombol agar tidak double-click
        binding.btnSimpan.isEnabled = false
        binding.btnSimpan.text = "Menyimpan..."
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val body = mapOf(
                    "nama" to nama,
                    "nim" to nim,
                    "jurusan_id" to jurusanId
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
