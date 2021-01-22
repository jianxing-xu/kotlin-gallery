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
        StringRequest(
            Request.Method.GET,
            getUrl(),
            { _photoList.value = Gson().fromJson(it, Pixabay::class.java).hits.toList() },
            {
                Toast.makeText(getApplication(), "请求错误", Toast.LENGTH_LONG).show()
                Log.d(TAG, "fetchData: $it")
            }
        ).also {
            // 添加到请求队列
            VolleySingleton.create(getApplication()).requestQueue.add(it);
        }
    }

    // 搜索关键字
    private val keyWords =
        arrayOf("cat", "dog", "car", "beauty", "phone", "computer", "flower", "animal");

    // 随机构造查询关键字Url
    private fun getUrl(): String {
        return "https://pixabay.com/api/?key=12472743-874dc01dadd26dc44e0801d61&q=${keyWords.random()}&per_page=100";
    }
}