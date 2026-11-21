    package com.a2511500012.Mahasiswa.model
    import com.google.gson.annotations.SerializedName

        data class MahasiswaResponse(
            @SerializedName("status")
            val status: Boolean,

            @SerializedName("message")
            val message: String,

            @SerializedName("data")
            val data: List<Mahasiswa>
        )

         data class Mahasiswa(
             @SerializedName("id")
             val id: String,

            @SerializedName("nim")
            val nim: String,

            @SerializedName("nama")
            val nama: String,

            @SerializedName("jurusan")
            val jurusan: String
         )

             // Untuk GET by ID: data = object tunggal, bukan list
         data class MahasiswaDetailResponse(
            @SerializedName("status")
             val status: Boolean,

             @SerializedName("message")
             val message: String? = null,

             @SerializedName("data")
             val data: Mahasiswa? = null

         )
