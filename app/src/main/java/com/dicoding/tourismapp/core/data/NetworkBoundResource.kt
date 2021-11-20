package com.dicoding.tourismapp.core.data

import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

//abstract class NetworkBoundResource<ResultType, RequestType>(private val mExecutors: AppExecutors) {
/*terapan clean architecture
private val result = MediatorLiveData<Resource<ResultType>>()

init {
    result.value = Resource.Loading(null)

    @Suppress("LeakingThis")
    val dbSource = loadFromDB()

    result.addSource(dbSource) { data ->
        result.removeSource(dbSource)
        if (shouldFetch(data)) {
            fetchFromNetwork(dbSource)
        } else {
            result.addSource(dbSource) { newData ->
                result.value = Resource.Success(newData)
            }
        }
    }
}

protected open fun onFetchFailed() {}

protected abstract fun loadFromDB(): LiveData<ResultType>

protected abstract fun shouldFetch(data: ResultType?): Boolean

protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>

protected abstract fun saveCallResult(data: RequestType)

private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {

    val apiResponse = createCall()

    result.addSource(dbSource) { newData ->
        result.value = Resource.Loading(newData)
    }
    result.addSource(apiResponse) { response ->
        result.removeSource(apiResponse)
        result.removeSource(dbSource)
        when (response) {
            is ApiResponse.Success ->
                mExecutors.diskIO().execute {
                    saveCallResult(response.data)
                    mExecutors.mainThread().execute {
                        result.addSource(loadFromDB()) { newData ->
                            result.value = Resource.Success(newData)
                        }
                    }
                }
            is ApiResponse.Empty -> mExecutors.mainThread().execute {
                result.addSource(loadFromDB()) { newData ->
                    result.value = Resource.Success(newData)
                }
            }
            is ApiResponse.Error -> {
                onFetchFailed()
                result.addSource(dbSource) { newData ->
                    result.value = Resource.Error(response.errorMessage, newData)
                }
            }
        }
    }
}

fun asLiveData(): LiveData<Resource<ResultType>> = result

 */
//terapan react 6
abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result = PublishSubject.create<Resource<ResultType>>()
    private val mCompositeDisposable = CompositeDisposable()

    init {
        val dbSource = loadFromDB()

        val db = dbSource
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe { value ->
                dbSource.unsubscribeOn(Schedulers.io())
                if (shouldFetch(value)) {
                    fetchFromNetwork()
                } else {
                    result.onNext(Resource.Success(value))
                }
            }
        mCompositeDisposable.add(db)

    }

    protected open fun onFetchFailed() {}

    protected abstract fun loadFromDB(): Flowable<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract fun createCall(): Flowable<ApiResponse<RequestType>>

    protected abstract fun saveCallResult(data: RequestType)


    private fun fetchFromNetwork() {

        val apiResponse = createCall()

        result.onNext(Resource.Loading(null))
        val response = apiResponse
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .doOnComplete {
                mCompositeDisposable.dispose()
            }
            .subscribe { response ->
                when(response){
                    is ApiResponse.Success ->{
                        saveCallResult(response.data)
                        val dbSource = loadFromDB()
                        dbSource.subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe {
                                dbSource.unsubscribeOn(Schedulers.io())
                                    result.onNext(Resource.Success(it))

                            }
                    }
                    is ApiResponse.Empty -> {
                        val dbSource = loadFromDB()
                        dbSource.subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .take(1)
                            .subscribe {
                                dbSource.unsubscribeOn(Schedulers.io())
                                result.onNext(Resource.Success(it))
                            }
                    }
                    is ApiResponse.Error ->{
                        onFetchFailed()
                        result.onNext(Resource.Error(response.errorMessage,null))
                    }
                }
            }
        mCompositeDisposable.addAll(response)
    }
    fun asFlowable():Flowable<Resource<ResultType>> =result.toFlowable(BackpressureStrategy.BUFFER)
}