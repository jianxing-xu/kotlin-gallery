package cn.xu.gallery.utils

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(context: Context) {
    companion object {
        private var INSTANCE: VolleySingleton? = null;
        fun create(context: Context) = INSTANCE ?: synchronized(this, fun(): VolleySingleton {
            return VolleySingleton(context).also { INSTANCE = it }
        })
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
}
