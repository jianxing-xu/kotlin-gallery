package cn.xu.gallery

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.toLiveData
import cn.xu.gallery.utils.VolleySingleton
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson


class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory(application)
    val pagedListPhoto = factory.toLiveData(100)

    val networdStatus = Transformations.switchMap(factory.pixabayDataSource) { it.networkStatus }

    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }

    fun resetQuery() {
        // 重新生成工厂对象，会重新初始化数据
        pagedListPhoto.value?.dataSource?.invalidate()
    }
}

