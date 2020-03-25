package com.example.coronadiagnosticapp.data.network

import com.example.coronadiagnosticapp.data.db.entity.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


const val BASE_URL = "https://tnj0200iy8.execute-api.eu-west-1.amazonaws.com/staging/"
const val SIGNUP_URL =
    "api/me/sign-up/"

const val FILL_DETAILS_URL = "api/me/fill-personal-info/"

const val DAILY_METRICS_URL = "api/me/fill-daily-metrics/"


@Singleton
interface ApiServer {
    @POST(SIGNUP_URL)
    fun registerUser(
        @Body user: UserRegister
    ): Deferred<ResponseUser>

    @PUT(FILL_DETAILS_URL)
    fun updateUserInformation(
        @Body user: User
    ): Deferred<User>

    @POST(DAILY_METRICS_URL)
    fun updateUserMetrics(
        @Body sendMetric: SendMetric
    ): Deferred<ResponseMetric>

    companion object {
        operator fun invoke(interceptor: TokenServiceInterceptor): ApiServer {
            val okHttpClient = OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
                .build()
                .create(ApiServer::class.java)
        }
    }
}


@Singleton
class TokenServiceInterceptor @Inject constructor(val sessionToken: String?) : Interceptor {
    val AUTH_HEDER_KEY = "Authorization"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if (sessionToken != null) {
            requestBuilder.addHeader(
                AUTH_HEDER_KEY, "JWT $sessionToken"
            )
        }
        return chain.proceed(requestBuilder.build())
    }
}