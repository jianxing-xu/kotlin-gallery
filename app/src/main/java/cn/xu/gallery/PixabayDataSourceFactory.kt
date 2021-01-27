package cn.xu.gallery

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

// 数据源工厂
class PixabayDataSourceFactory(val context: Context) : DataSource.Factory<Int, PhotoItem>() {
    private val _pixabayDataSource: MutableLiveData<PixabayDataSource> = MutableLiveData()
    val pixabayDataSource: MutableLiveData<PixabayDataSource> get() = _pixabayDataSource
    override fun create(): DataSource<Int, PhotoItem> {
        return PixabayDataSource(context).also { _pixabayDataSource.postValue(it) }
    }
}