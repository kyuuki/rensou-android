package jp.kyuuki.rensou.android.net;

import jp.kyuuki.rensou.android.common.Logger;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Volley。
 * 
 * - Volley 関連で一元管理するものをここに。
 */
public final class VolleyUtils {
    private static final String TAG = VolleyUtils.class.getName();

    public static RequestQueue mRequestQueue = null;  // RequestQueue をアプリで 1 つに。null になる場合があるので注意!

    private VolleyUtils() {};

    // 基本は mRequestQueue が null になる可能性があるので、
    public static RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            Logger.d(TAG, "newRequestQueue");
            mRequestQueue = Volley.newRequestQueue(context);
        }
        
        return mRequestQueue;
    }
}
