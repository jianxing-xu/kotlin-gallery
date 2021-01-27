package cn.xu.gallery

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import cn.xu.gallery.utils.VolleySingleton
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlin.math.ceil

enum class NetWorkState {
    INIT_LOAD,
    LOADING,
    LOADED,
    FAILED,
    COMPLETE
}

// 数据源
class PixabayDataSource(private val context: Context) : PageKeyedDataSource<Int, PhotoItem>() {

    // 当网络错误时保存重试函数
    var retry: (() -> Any)? = null
    val perPage = 100

    // 网络状态
    private val _networkStatus: MutableLiveData<NetWorkState> = MutableLiveData()
    val networkStatus: LiveData<NetWorkState> get() = _networkStatus

    private val queryKey =
        arrayOf("cat", "beautiful", "car", "photo", "person", "girl", "anime").random()
//    private val queryKey = arrayOf("无法无天").random()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, PhotoItem>
    ) {
        // 发起请求时，将重试函数置空
        _networkStatus.postValue(NetWorkState.INIT_LOAD)
        retry = null
        val url =
            "https://api.pexels.com/v1/search?query=${queryKey}&per_page=${perPage}&page=${1}"
        Log.d("MY_DE", "初始化URL: $url ")
        object : StringRequest(
            Method.GET,
            url,
            {
                // request success
                val result = Gson().fromJson(it, Pixabay::class.java)
                callback.onResult(result.hits.toList(), null, 2)
                Log.d("MY_DE", "总页数： ${result.totalHits / perPage} ")
                _networkStatus.postValue(NetWorkState.LOADED)
            },
            {
                // 失败时，保存当前失败函数
                retry = { loadInitial(params, callback) }
                // request fail
                _networkStatus.postValue(NetWorkState.FAILED)
                Log.d("MY_DE", "loadAfter: 请求失败")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf("Authorization" to "563492ad6f91700001000001b801c7451e82434d924781e2bc999f1a")
            }
        }.also {
            VolleySingleton.create(context).requestQueue.add(it)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
        retry = null
        _networkStatus.postValue(NetWorkState.LOADING)
        val url =
            "https://api.pexels.com/v1/search?query=${queryKey}&per_page=${perPage}&page=${params.key}"
        Log.d("MY_DE", "下一页URL: $url ")
        object : StringRequest(
            Method.GET,
            url,
            {
                // request success
                val result = Gson().fromJson(it, Pixabay::class.java)
                val totalPage = ceil(result.totalHits.toDouble() / perPage).toInt()
                if (params.key > totalPage) {
                    _networkStatus.postValue(NetWorkState.COMPLETE)
                } else {
                    callback.onResult(result.hits.toList(), params.key + 1)
                }
                _networkStatus.postValue(NetWorkState.LOADED)
            },
            {
                retry = { loadAfter(params, callback) }
                // request fail
                _networkStatus.postValue(NetWorkState.FAILED)
                Log.d("MY_DE", "loadAfter: 请求失败 $it")
            }

        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf("Authorization" to "563492ad6f91700001000001b801c7451e82434d924781e2bc999f1a")
            }
        }.also {
            VolleySingleton.create(context).requestQueue.add(it)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PhotoItem>) {
        TODO("Not yet implemented")
    }

}