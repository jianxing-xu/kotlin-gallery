package cn.xu.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PagerPhotoPage : Fragment() {
    private var viewPager2: ViewPager2? = null
    var viewPagerTag: TextView? = null
    private var saveBtn: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflate = inflater.inflate(R.layout.fragment_pager_photo_page, container, false)
        viewPager2 = inflate.findViewById(R.id.veiwPager2)
        viewPagerTag = inflate.findViewById(R.id.pagerTag)
        saveBtn = inflate.findViewById(R.id.download)
        return inflate
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val photoList = arguments?.getParcelableArrayList<PhotoItem>("PHOTO_LIST")
        val pos = arguments?.getInt("PHOTO_POS")
        PagerPhotoAdapter().apply {
            viewPager2?.adapter = this
            submitList(photoList)
        }
        viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPagerTag?.text = "$position / ${photoList?.size}"
            }
        })
        viewPager2?.setCurrentItem(pos!!, false)

        saveBtn?.setOnClickListener {
            if (Build.VERSION.SDK_INT < 29 && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    savePhoto()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        savePhoto()
                    }
                } else {
                    Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // 保存图片
    private suspend fun savePhoto() {
        // IO 线程执行耗时操作
        withContext(Dispatchers.IO) {
            ((viewPager2?.get(0) as RecyclerView).findViewHolderForAdapterPosition(viewPager2?.currentItem!!) as PagerPhotoViewHolder).apply {
                var bitmap = imageView?.drawable?.toBitmap()
                // 声明保存路径
                val saveUri =
                    requireContext().contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ContentValues()
                    ) ?: kotlin.run {
                        MainScope().launch {
                            Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext
                    }

                // 保存
                requireContext().contentResolver.openOutputStream(saveUri).use {
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }.apply {
                    val that = this
                    // 主线程执行Toast
                    MainScope().launch {
                        if (that as Boolean) {
                            Toast.makeText(requireContext(), "存储成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "存储失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }


            }
        }
    }

}