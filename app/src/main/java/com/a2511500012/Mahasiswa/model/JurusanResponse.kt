
package com.a2511500012.Mahasiswa.model

import com.google.gson.annotations.SerializedName

data class JurusanResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Jurusan>
)

data class Jurusan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("kode_jurusan")
    val kodeJurusan: String,

    @SerializedName("nama_jurusan")
    val namaJurusan: String,

    @SerializedName("fakultas")
    val fakultas: String
)
