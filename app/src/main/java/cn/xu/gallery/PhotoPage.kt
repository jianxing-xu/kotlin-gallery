package cn.xu.gallery

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide.with
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.supercharge.shimmerlayout.ShimmerLayout
import uk.co.senab.photoview.PhotoView

class PhotoPage : Fragment() {
    private var shimmerLayout: ShimmerLayout? = null
    private var photoItem: PhotoItem? = null
    private var photoView: PhotoView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 获取传过来的参数
        arguments?.getParcelable<PhotoItem>("PHOTO").also { photoItem = it }
        Log.d("MY_DE", "onCreateView: ${photoItem?.fullUrl}")
        return inflater.inflate(R.layout.fragment_photo_page, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        shimmerLayout = requireActivity().findViewById(R.id.shimmerLayoutPhoto)
        photoView = shimmerLayout?.findViewById(R.id.photoView)
        shimmerLayout?.apply {
            setShimmerColor(0x55FFFFFF)
            setShimmerAngle(45)
            startShimmerAnimation()
        }
        with(this)
            .load(photoItem?.fullUrl)
            .placeholder(R.drawable.ic_baseline_insert_photo_24)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    shimmerLayout?.stopShimmerAnimation()
                    return false
                }

            }).into(photoView!!)
    }
}