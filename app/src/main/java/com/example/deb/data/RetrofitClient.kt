package com.example.deb.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 에뮬레이터에서 호스트 PC의 8000번 FastAPI 를 부를 때 이 주소
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // OkHttpClient 에 충분한 타임아웃을 설정
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // 연결 시도 최대 30초
        .readTimeout(120, TimeUnit.SECONDS)     // 응답 대기 최대 60초
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)  // <-- 여기에 OkHttpClient 물려주기
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
