
package com.a2511500012.Mahasiswa

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.a2511500012.Mahasiswa.databinding.ActivityMainBinding
import com.a2511500012.Mahasiswa.api.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cek koneksi jika btnTest diklik
        binding.btnTestConnection.setOnClickListener {
            cekKoneksiApi()
        }
    }

    private fun cekKoneksiApi() {
        // 1. Tampilkan indikator loading & status awal
        binding.progressBar.visibility = View.VISIBLE
        binding.btnTestConnection.isEnabled = false
        updateStatus("Status: Menghubungkan ke server...", Color.DKGRAY)

        // 2. Jalankan request jaringan secara asynchronous menggunakan Coroutine
        lifecycleScope.launch {
            try {
                // Eksekusi panggilan Retrofit di thread IO
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().getDaftarMahasiswa()
                }

                // 3. Penanganan HTTP Response
                if (response.isSuccessful) {
                    val body = response.body()
                    val jumlahData = body?.data?.size ?: 0

                    updateStatus(
                        "Status: Terhubung (HTTP ${response.code()})\n" +
                                "Pesan: ${body?.message ?: "OK"} ($jumlahData data ditemukan)",
                        Color.parseColor("#2E7D32")
                    )
                } else {
                    // Server merespons, namun mengembalikan kode error
                    if (response.code() == 404) {
                        updateStatus(
                            "Status: 404 - Nama file atau path API salah. Periksa URL endpoint.",
                            Color.parseColor("#C62828")
                        )
                    } else {
                        updateStatus(
                            "Status: Gagal merespons (HTTP ${response.code()} - ${response.message()})",
                            Color.parseColor("#C62828")
                        )
                    }
                }

            } catch (e: SocketTimeoutException) {
                // 4. Penanganan Exception: Waktu koneksi habis
                updateStatus(
                    "Status: Timeout - Periksa firewall, koneksi Wi-Fi, dan port server.",
                    Color.parseColor("#C62828")
                )

            } catch (e: IOException) {
                // Penanganan Exception: Tidak ada internet / server mati / salah IP
                updateStatus(
                    "Status: Gagal terhubung - Periksa IP 10.0.2.2, BASE_URL, port, dan pastikan server berjalan.",
                    Color.parseColor("#C62828")
                )

            } catch (e: HttpException) {
                // Penanganan Exception: Kesalahan protokol HTTP
                updateStatus(
                    "Status: Kesalahan HTTP (${e.code()})",
                    Color.parseColor("#C62828")
                )

            } catch (e: Exception) {
                // Penanganan Exception umum lainnya
                updateStatus(
                    "Status: Terjadi kesalahan (${e.localizedMessage})",
                    Color.parseColor("#C62828")
                )
            } finally {
                // Sembunyikan loading dan aktifkan kembali tombol
                binding.progressBar.visibility = View.GONE
                binding.btnTestConnection.isEnabled = true
            }
        }
    }

    // 5. Fungsi pembaruan status koneksi pada UI
    private fun updateStatus(pesan: String, warna: Int) {
        binding.tvConnectionStatus.text = pesan
        binding.tvConnectionStatus.setTextColor(warna)
    }
}
