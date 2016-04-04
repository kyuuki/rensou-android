package jp.kyuuki.rensou.android.commons;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Volley ユーティリティ。
 *
 * - Volley 関連で一元管理するものをここに。
 * - Volley はオワコンかも。
 */
public final class VolleyUtils {
    private static final String TAG = VolleyUtils.class.getName();

    public static RequestQueue mRequestQueue = null;  // RequestQueue をアプリで 1 つに。null になる場合があるので注意!
    public static ImageLoader mImageLoader = null;

    private VolleyUtils() {}

    public static RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            Logger.d(TAG, "newRequestQueue()");
            mRequestQueue = Volley.newRequestQueue(context);
        }

        return mRequestQueue;
    }

    public static ImageLoader getImageLoader(Context context) {
        if (mImageLoader == null) {
            Logger.d(TAG, "newImageLoader()");
            mImageLoader = new ImageLoader(getRequestQueue(context), new BitmapCache());
        }

        return mImageLoader;
    }
}

