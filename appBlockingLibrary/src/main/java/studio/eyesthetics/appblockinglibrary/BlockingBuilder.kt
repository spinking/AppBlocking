package studio.eyesthetics.appblockinglibrary

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
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
import retrofit2.http.Url
import timber.log.Timber

class BlockingBuilder() {
    var compositeDisposable = CompositeDisposable()

    private var baseUrl = "https://google.com"
    private var endPoint = "/"

    fun setBaseUrl(baseUrl: String) : BlockingBuilder {
        this.baseUrl = baseUrl
        return this
    }

    fun setEndPoint(relativeUrl: String) : BlockingBuilder {
        this.endPoint = relativeUrl
        return this
    }

    fun blockingRequest(activity: Activity) {

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient =
            OkHttpClient.Builder().addInterceptor(interceptor).build()

        val apiBlocking =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(ApiBlocking::class.java) as ApiBlocking


        val disposable =
            apiBlocking.blockingState(endPoint)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.not()) {
                        activity.setContentView(R.layout.blocking_layout)
                        stopServices(activity)
                    }
                }, {
                    Timber.e(it)
                })

        compositeDisposable.add(disposable)
    }

    private fun stopServices(activity: Activity) {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val rs =
            am.getRunningServices(50)
        var message: String? = null
        for (i in rs.indices) {
            val rsi = rs[i] as ActivityManager.RunningServiceInfo
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
        @GET
        fun blockingState(@Url url: String) : Observable<Boolean>
    }
}