package cn.xu.gallery

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
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
    private var galleryViewModel: GalleryViewModel? = null

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
                galleryViewModel?.needToTop = true
                // 使用协程 至少加载一秒
                GlobalScope.launch {
                    delay(1000)
                    galleryViewModel?.reset()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 显示菜单
        setHasOptionsMenu(true)
        // 初始化viewmodel
        galleryViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(GalleryViewModel::class.java)
        // 创建适配器
        val galleryAdapter = GalleryAdapter(galleryViewModel)
        // 获取下拉刷新布局
        swipeRefreshLayout = requireActivity().findViewById(R.id.swipeRefreshLayout)
        // 获取recyclerview
        recyclerView = requireActivity().findViewById(R.id.recyclerView)
        // 配置 recyclerview
        recyclerView?.apply {
            // 添加适配器
            adapter = galleryAdapter
            // 指定 recyclerview 布局为 Grid
//            layoutManager = GridLayoutManager(requireContext(), 2)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        // 监视数据列表
        galleryViewModel?.photoList?.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)
            swipeRefreshLayout?.isRefreshing = false
            if (galleryViewModel?.needToTop == true) {
                recyclerView?.scrollToPosition(0)
                galleryViewModel?.needToTop = false
            }
        })

        // 监听加载状态改变
        galleryViewModel?.loadState?.observe(viewLifecycleOwner, {
            // 改变加载状态
            galleryAdapter.footerViewStatus = it
            // 手动通知适配更新UI
            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount - 1)
            if (it === LoadState.LOAD_ERR) {
                swipeRefreshLayout?.isRefreshing = false
            }
        })

        swipeRefreshLayout?.setOnRefreshListener {
            galleryViewModel?.reset()
        }

        // 监听滚动，滚动到最后一项加载更多数据
        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) return
                // 最后两项可见时 加载数据
                val intArr = IntArray(2)
                var viewArr =
                    (recyclerView.layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(
                        intArr
                    )
                if (viewArr[0] == galleryAdapter.itemCount - 1) {
                    galleryViewModel?.fetchData()
                }
            }
        })
    }

}