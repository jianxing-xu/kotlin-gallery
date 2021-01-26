package cn.xu.gallery

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// 适配器就干适配器该干的就 OK 了

class PagerPhotoAdapter :
    ListAdapter<PhotoItem, PagerPhotoViewHolder>(object : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerPhotoViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.photo_item_pager, parent, false)
            .let {
                val holder = PagerPhotoViewHolder(it)
                holder
            }
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: PagerPhotoViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(getItem(position)?.src?.fullUrl)
            .placeholder(R.drawable.ic_baseline_insert_photo_24)
            .into(holder.imageView!!)
    }

}


class PagerPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView? = itemView.findViewById(R.id.imageView)
}