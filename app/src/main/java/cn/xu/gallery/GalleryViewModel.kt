package cn.xu.gallery

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.xu.gallery.utils.VolleySingleton
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson


class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG: String = "ERROR"
    }

    private val _photoList = MutableLiveData<List<PhotoItem>>();
    val photoList: LiveData<List<PhotoItem>>
        get() = _photoList

    // 获取数据
    fun fetchData() {
        // 构建 StringRequest
        object : StringRequest(
            Method.GET,
            getUrl(),
            { _photoList.value = Gson().fromJson(it, Pixabay::class.java).hits.toList() },
            {
                Toast.makeText(getApplication(), "请求错误", Toast.LENGTH_LONG).show()
                Log.d(TAG, "fetchData: $it")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "563492ad6f91700001000001b801c7451e82434d924781e2bc999f1a")
            }
        }.also {
            // 添加到请求队列
            VolleySingleton.create(getApplication()).requestQueue.add(it);
        }
    }

    // 搜索关键字
    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal");

    // 随机构造查询关键字Url
    private fun getUrl(): String {
        return "https://api.pexels.com/v1/search?query=${keyWords.random()}&per_page=${100}&page=${1}";
    }
}

