package cn.xu.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.supercharge.shimmerlayout.ShimmerLayout


/**
 *
 * 全局函数：getItem(pos) 获取通过索引获取某一项惨淡的数据
 */

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class GalleryAdapter(
    private var netWorkState: NetWorkState? = null,
    private val galleryViewModel: GalleryViewModel? = null
) :
    PagedListAdapter<PhotoItem, RecyclerView.ViewHolder>(DiffCallback) {
    private var hasFooter = false

    // 重新进入这个页面时,自动重试
    init {
        galleryViewModel?.retry()
    }

    // 更新网络状态
    fun updateNetWorkState(netWorkState: NetWorkState?) {
        if (netWorkState == NetWorkState.INIT_LOAD) hideFooter() else showFooter()
        this.netWorkState = netWorkState
    }

    // 隐藏底部加载
    private fun hideFooter() {
        if (hasFooter) {
            notifyItemChanged(itemCount - 1)
        }
        hasFooter = false
    }

    // 显示底部加载
    private fun showFooter() {
        if (hasFooter) {
            notifyItemRemoved(itemCount - 1)
        } else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }

    // 针对 footer_cell 调整数量
    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasFooter) 1 else 0
    }

    // 针对 footer_cell 调整类型
    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1 && hasFooter) R.layout.footer_cell else R.layout.gallery_cell
    }

    // 创建一个列表项
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.gallery_cell -> {
                PhotoViewHolder.create(parent).also { holder ->
                    // 给列表项添加点击事件
                    holder.itemView.setOnClickListener {
                        val bundle = Bundle().apply {
                            putParcelable("PHOTO", getItem(holder.adapterPosition))
                        }
                        // 导航到第二个页面
                        holder.itemView.findNavController()
                            .navigate(R.id.action_galleryPage_to_photoPage, bundle)
                    }
                }
            }
            else -> {
                FooterViewHolder.create(parent).also { holder ->
                    holder.loadingText?.setOnClickListener {
                        galleryViewModel?.retry()
                    }
                }
            }
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == R.layout.footer_cell) {
            (holder as FooterViewHolder).bindWithNetworkStatus(netWorkState)
        } else {
            val photo = getItem(position) ?: return
            (holder as PhotoViewHolder).bindViewHolder(photo)
        }
    }

    // 比较回调
    object DiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean =
            oldItem.photoId === newItem.photoId

    }


}

// 自定义ViewHolder
class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.photoView)
    val shimmerCell: ShimmerLayout = itemView.findViewById(R.id.shimmer_cell)

    companion object {
        fun create(parent: ViewGroup): PhotoViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_cell, parent, false)
            return PhotoViewHolder(view)
        }
    }

    fun bindViewHolder(photo: PhotoItem?) {
        // 设置闪动参数
        shimmerCell.apply {
            // 设置闪动颜色
            setShimmerColor(0x55FFFFFF)
            // 设置闪动角度
            setShimmerAngle(45)
            // 开始闪动
            startShimmerAnimation()
        }
        // 加载图片
        Glide.with(itemView)
            .load(photo?.src?.previewUrl)
            .placeholder(R.drawable.ic_baseline_insert_photo_24)
            // 监听图片加载是否完成
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also { shimmerCell.stopShimmerAnimation() }
                }

            })
            .into(imageView)
    }
}

// 底部加载
class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val loadingText: TextView? = itemView.findViewById(R.id.loadingText)
    private val loadingBar: ProgressBar? = itemView.findViewById(R.id.loadingBar)

    companion object {
        fun create(parent: ViewGroup): FooterViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.footer_cell, parent, false)
            (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
            return FooterViewHolder(view)
        }
    }

    fun bindWithNetworkStatus(netWorkState: NetWorkState?) {
        when (netWorkState) {
            NetWorkState.FAILED -> {
                loadingBar?.visibility = View.GONE
                loadingText?.text = "加载失败，点击重试"
                loadingText?.isClickable = true
            }
            NetWorkState.LOADING -> {
                loadingBar?.visibility = View.VISIBLE
                loadingText?.text = "正在加载"
                loadingText?.isClickable = false
            }
            NetWorkState.COMPLETE -> {
                loadingBar?.visibility = View.GONE
                loadingText?.text = "已经到底了"
                loadingText?.isClickable = false
            }
            else -> {
            }
        }
    }
}
