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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlin.random.Random


/**
 *
 * 全局函数：getItem(pos) 获取通过索引获取某一项惨淡的数据
 */

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class GalleryAdapter(var galleryViewModel: GalleryViewModel?) :
    ListAdapter<PhotoItem, MyViewHolder>(DiffCallback) {

    var footerViewStatus = LoadState.LOAD_MORE

    companion object {
        // 底部loading
        const val FOOTER_TYPE = 0
        // 正常图片
        const val NORMAL_TYPE = 1
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position === itemCount - 1) FOOTER_TYPE else NORMAL_TYPE
    }

    // 创建一个列表项
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var holder: MyViewHolder
        if (viewType === NORMAL_TYPE) {
            holder = MyViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_cell, parent, false)
            )
            // 给列表项添加点击事件
            holder.itemView.setOnClickListener {
                val photo: PhotoItem = getItem(holder.adapterPosition)
                val bundle = Bundle().apply {
//                putParcelable("PHOTO", photo)
                    // 传入图片列表
                    putParcelableArrayList("PHOTO_LIST", ArrayList(currentList))
                    // 传入当前索引
                    putInt("PHOTO_POS", holder.adapterPosition)
                }
                // 导航到第二个页面
//            holder.itemView.findNavController()
//                .navigate(R.id.action_galleryPage_to_photoPage, bundle)
                holder.itemView.findNavController()
                    .navigate(R.id.action_galleryPage_to_pagerPhotoPage, bundle)
            }
        } else {
            holder = MyViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.gallery_footer, parent, false)
                    .also {
                        // 设置加载控件占满一行
                        (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
                            true
                    }
            ).apply {
                // 点击错误消息重试
                loadingText?.setOnClickListener {
                    // 把加载状态设置成 loading
                    galleryViewModel?.loadState?.value = LoadState.LOAD_MORE
                    galleryViewModel?.fetchData()
                }
            }
        }
        return holder
    }

    // 给列表每一项绑定数据
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position === itemCount - 1) {
            when (footerViewStatus) {
                LoadState.LOAD_MORE -> {
                    holder.loadProgress?.visibility = View.VISIBLE
                    holder.loadingText?.text = "正在加载"
                    holder.loadingText?.isClickable = false
                }
                LoadState.NO_MORE -> {
                    holder.loadProgress?.visibility = View.GONE
                    holder.loadingText?.text = "我是有底线的"
                    holder.loadingText?.isClickable = false
                }
                LoadState.LOAD_ERR -> {
                    holder.loadProgress?.visibility = View.GONE
                    holder.loadingText?.text = "网络错误 点击重试"
                    // 只有在网络错误的时候才能点击
                    holder.loadingText?.isClickable = true
                }
            }
            return
        }
        val item = getItem(position)
        // 设置闪动参数
        holder.apply {
            shimmerCell?.apply {
                // 设置闪动颜色
                setShimmerColor(0x55FFFFFF)
                // 设置闪动角度
                setShimmerAngle(45)
                // 开始闪动
                startShimmerAnimation()
            }
            imageView?.layoutParams?.height = item.height / 10
            good?.text = "${item.height / Random.nextInt(1, 6)}"
            fav?.text = "${item.height / Random.nextInt(4, 7)}"
            creator?.text = item.name
        }
        // 加载图片
        Glide.with(holder.itemView)
            .load(getItem(position).src?.previewUrl)
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
                    return false.also { holder.shimmerCell?.stopShimmerAnimation() }
                }

            })
            .into(holder.imageView!!)
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
class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView? = itemView.findViewById(R.id.photoView)
    val shimmerCell: ShimmerLayout? = itemView.findViewById(R.id.shimmer_cell)
    val good: TextView? = itemView.findViewById(R.id.good)
    val fav: TextView? = itemView.findViewById(R.id.fav)
    val creator: TextView? = itemView.findViewById(R.id.creator)
    val loadProgress: ProgressBar? = itemView.findViewById(R.id.progressBar)
    val loadingText: TextView? = itemView.findViewById(R.id.loadingText)
//    val cardView: CardView = itemView.findViewById(R.id.card_cell)
}