package com.a2511500012.Mahasiswa.api

import com.a2511500012.Mahasiswa.model.MahasiswaDetailResponse
import com.a2511500012.Mahasiswa.model.MahasiswaPostResponse
import com.a2511500012.Mahasiswa.model.MahasiswaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body

interface ApiInterface {

     @GET("mahasiswa.php")
     suspend fun getDaftarMahasiswa(): Response<MahasiswaResponse>

     @GET("mahasiswa.php")
     suspend fun searchMahasiswa(
         @Query("search") keyword: String
     ): Response<MahasiswaResponse>

     @GET("mahasiswa.php")
     suspend fun getDetailMahasiswa(
        @Query("id") id: String
     ): Response<MahasiswaDetailResponse>

    @POST("mahasiswa.php")
    suspend fun addMahasiswa(
        @Body body: Map<String, String>
     ): Response<MahasiswaPostResponse>
}