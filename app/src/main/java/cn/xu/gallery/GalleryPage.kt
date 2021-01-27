package cn.xu.gallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GalleryPage : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private val galleryViewModel by viewModels<GalleryViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery_page, container, false)
    }

    // 创建菜单
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    // 菜单项被点击事件处理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pullDown -> {
                swipeRefreshLayout?.isRefreshing = true
                // 使用协程 至少加载一秒
                GlobalScope.launch {
                    delay(1000)
                    galleryViewModel.resetQuery()
                }
            }
            R.id.retry -> {
                galleryViewModel.retry()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 显示菜单
        setHasOptionsMenu(true)
        // 创建适配器
        val galleryAdapter = GalleryAdapter(galleryViewModel = galleryViewModel)
        // 获取下拉刷新布局
        swipeRefreshLayout = requireActivity().findViewById(R.id.swipeRefreshLayout)
        // 获取recyclerview
        recyclerView = requireActivity().findViewById(R.id.recyclerView)
        // 配置 recyclerview
        recyclerView?.apply {
            // 添加适配器
            adapter = galleryAdapter
            // 指定 recyclerview 布局为 Grid
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        galleryViewModel.pagedListPhoto.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)
            swipeRefreshLayout?.isRefreshing = false
        })

        // 监视网络加载状态
        galleryViewModel.networdStatus.observe(viewLifecycleOwner, {
            Log.d("MY_DE", "网络状态：$it ")
            galleryAdapter.updateNetWorkState(it)
            // 第一次进来时显示加载中
            swipeRefreshLayout?.isRefreshing = it == NetWorkState.INIT_LOAD
        })



        swipeRefreshLayout?.setOnRefreshListener {
            galleryViewModel.resetQuery()
        }
    }

}