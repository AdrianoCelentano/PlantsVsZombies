package com.clean.plantsvszombies

import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


interface ZombieService {
    @GET("/plants")
    fun getPlants(): Single<List<Plant>>

    @POST("/plants")
    fun createPlant(@Body plant: Plant): Completable
}

object ZombieServiceFactory {

    fun provideZombieService(): ZombieService {
        val okHttpClient = provideOkHttpClient()
        val gson = Gson()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pure-anchorage-63206.herokuapp.com")
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(ZombieService::class.java)
    }

    private fun provideIsDebug() = BuildConfig.DEBUG

    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = provideLoggingInterceptor()
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val isDebug = provideIsDebug()
        val logging = HttpLoggingInterceptor()
        logging.level = if (isDebug) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }
}