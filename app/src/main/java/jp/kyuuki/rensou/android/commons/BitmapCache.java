package jp.kyuuki.rensou.android.commons;

import android.graphics.Bitmap;
import androidx.collection.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

// http://dev.classmethod.jp/smartphone/android/android-tips-51-volley/
public class BitmapCache implements ImageCache {

    private LruCache<String, Bitmap> mCache;

    public BitmapCache() {
        int maxSize = 10 * 1024 * 1024;
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    public Bitmap getBitmap(String url) {
        return mCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mCache.put(url, bitmap);
    }
}

