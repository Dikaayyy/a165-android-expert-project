package com.dicoding.tourismapp.core.data.source.remote


import android.util.Log
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.data.source.remote.network.ApiService
import com.dicoding.tourismapp.core.data.source.remote.response.ListTourismResponse
import com.dicoding.tourismapp.core.data.source.remote.response.TourismResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject


class RemoteDataSource private constructor(private val apiService: ApiService) {
    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(service: ApiService): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource(service)
            }
    }

    fun getAllTourism(): Flowable<ApiResponse<List<TourismResponse>>> {
        val resultData = PublishSubject.create<ApiResponse<List<TourismResponse>>>()

        //get data from local json
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            try {
//                val dataArray = jsonHelper.loadData()
//                if (dataArray.isNotEmpty()) {
//                    resultData.value = ApiResponse.Success(dataArray)
//                } else {
//                    resultData.value = ApiResponse.Empty
//                }
//            } catch (e: JSONException){
//                resultData.value = ApiResponse.Error(e.toString())
//                Log.e("RemoteDataSource", e.toString())
//            }
//        }, 2000)
//
//        return resultData

        // get data from remote api
        val client = apiService.getList()

            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe ({ response ->
                val dataArray = response.places
                resultData.onNext(if (dataArray.isNotEmpty()) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            }, { error ->
                resultData.onNext(ApiResponse.Error(error.message.toString()))
                Log.e("RemoteDataSource", error.toString())
            })
        return resultData.toFlowable(BackpressureStrategy.BUFFER)
    }
}

