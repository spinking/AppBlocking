package studio.eyesthetics.appblockinglibrary

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class AppBlockingModule {

    var compositeDisposable = CompositeDisposable()

    fun blockingRequest(activity: Activity, service: Class<*>?) {
        Log.d("LOG_TAG", "blockingRequest")

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient =
            OkHttpClient.Builder().addInterceptor(interceptor).build()
        val apiBlocking =
            Retrofit.Builder().baseUrl("https://google.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
                .create(
                    ApiBlocking::class.java
                ) as ApiBlocking
        Log.d("LOG_TAG", "ApiBlocking setContentView + sleep")
        val disposable =
            apiBlocking.blockingState
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { complete: Boolean? ->
                        Log.d(
                            "LOG_TAG",
                            "message not blocking"
                        )
                    }
                ) { error: Throwable? ->
                    Log.d("LOG_TAG", "message error + set content view + stop service")
                    activity.setContentView(R.layout.blocking_layout)
                    stopServices(activity)
                }
        compositeDisposable.add(disposable)
    }

    private fun stopServices(activity: Activity) {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val rs =
            am.getRunningServices(50)
        Log.d("LOG_TAG", "size " + rs.size)
        var message: String? = null
        for (i in rs.indices) {
            val rsi =
                rs[i] as ActivityManager.RunningServiceInfo
            Log.d(
                "LOG_TAG",
                "Process " + rsi.process + " with component " + rsi.service.className
            )
            message += rsi.process
            activity.stopService(
                Intent(
                    activity,
                    (rs[i] as ActivityManager.RunningServiceInfo).javaClass
                )
            )
        }
    }

    interface ApiBlocking {
        @get:GET("/block")
        val blockingState: Observable<Boolean>
    }
}