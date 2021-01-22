package cn.xu.gallery

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
class GalleryAdapter : ListAdapter<PhotoItem, MyViewHolder>(DiffCallback) {

    // 创建一个列表项
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val holder = MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_cell, parent, false)
        )
        // 给列表项添加点击事件
        holder.itemView.setOnClickListener {
            val photo: PhotoItem = getItem(holder.adapterPosition)
            val bundle = Bundle().apply {
                putParcelable("PHOTO", photo)
            }
            // 导航到第二个页面
            holder.itemView.findNavController()
                .navigate(R.id.action_galleryPage_to_photoPage, bundle)
        }
        return holder
    }

    // 给列表每一项绑定数据
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // 设置闪动参数
        holder.shimmerCell.apply {
            // 设置闪动颜色
            setShimmerColor(0x55FFFFFF)
            // 设置闪动角度
            setShimmerAngle(45)
            // 开始闪动
            startShimmerAnimation()
        }
        // 加载图片
        Glide.with(holder.itemView)
            .load(getItem(position).previewUrl)
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
                    return false.also { holder.shimmerCell.stopShimmerAnimation() }
                }

            })
            .into(holder.imageView)
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
    val imageView: ImageView = itemView.findViewById(R.id.photoView)
    val shimmerCell: ShimmerLayout = itemView.findViewById(R.id.shimmer_cell)
//    val cardView: CardView = itemView.findViewById(R.id.card_cell)
}