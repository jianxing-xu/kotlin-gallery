package cn.xu.gallery

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.xu.gallery.utils.VolleySingleton
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import kotlin.math.ceil

enum class LoadState {
    LOAD_MORE,
    NO_MORE,
    LOAD_ERR
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG: String = "MY_DE"
    }

    // 加载的状态
    var loadState: MutableLiveData<LoadState> = MutableLiveData()

    // 内部列表数据
    private val _photoList = MutableLiveData<List<PhotoItem>>();

    // 外部列表数据
    val photoList: LiveData<List<PhotoItem>>
        get() = _photoList

    var needToTop = true

    // 搜索关键字
    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal");
    // 请求状态
    private val perPage = 400
    private var currentPage = 1
    private var totalPage = 1
    private var isNew = true
    private var isLoading = false
    private var currentKey = "def"

    // 初始化数据
    init {
        reset()
    }

    // 初始化数据
    fun reset() {
        currentPage = 1
        totalPage = 1
        isNew = true
        isLoading = false
        currentKey = keyWords.random()
        fetchData()
    }

    // 获取数据
    fun fetchData() {
        if (isLoading) return
        if (currentPage > totalPage) {
            loadState.value = LoadState.NO_MORE
            return
        }
        isLoading = true
        // 构建 StringRequest
        object : StringRequest(
            Method.GET,
            getUrl(),
            {
                val pixable = Gson().fromJson(it, Pixabay::class.java)
                val list = pixable.hits.toList()
                if (isNew) {
                    _photoList.value = list
                } else {
                    _photoList.value = _photoList.value?.plus(list)
                }
                totalPage = ceil(pixable.totalHits.toDouble() / perPage).toInt()
                isLoading = false
                isNew = false
                currentPage++
                loadState.value = LoadState.LOAD_MORE
            },
            {
                isLoading = false
                Toast.makeText(getApplication(), "请求错误", Toast.LENGTH_LONG).show()
                loadState.value = LoadState.LOAD_ERR
                Log.d(TAG, "fetchData: $it")
            },
        ) {
            // 覆盖getHeaders 方法添加 API KEY 认证
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "563492ad6f91700001000001b801c7451e82434d924781e2bc999f1a")
            }
        }.also { request ->
            // 添加到请求队列
            VolleySingleton.create(getApplication()).requestQueue.add(request)
            Log.d(TAG, "fetchData: headers: ${request.headers}")
        }

        Log.d(
            TAG,
            "\n URL: ${getUrl()} \n currentPage: $currentPage \n isLoading: $isLoading \n isNew: $isNew \n totalPage: $totalPage"
        )
    }


    // 随机构造查询关键字Url
    private fun getUrl(): String {
        return "https://api.pexels.com/v1/search?query=${currentKey}&per_page=${perPage}&page=$currentPage";
    }
}