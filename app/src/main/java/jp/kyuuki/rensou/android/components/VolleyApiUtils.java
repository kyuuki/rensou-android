package jp.kyuuki.rensou.android.components;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.commons.VolleyUtils;
import jp.kyuuki.rensou.android.components.api.RensouApi;

/**
 * Volley ライブラリと API のブリッジ。
 *
 * - API に依存しないものは VolleyUtils へ。
 */
public class VolleyApiUtils {

    static Map<RensouApi.Method, Integer> methodMap;

    static {
        methodMap = new HashMap<>();
        methodMap.put(RensouApi.Method.GET,     Request.Method.GET);
        methodMap.put(RensouApi.Method.POST,    Request.Method.POST);
        methodMap.put(RensouApi.Method.PUT,     Request.Method.PUT);
        methodMap.put(RensouApi.Method.DELETE,  Request.Method.DELETE);
        methodMap.put(RensouApi.Method.HEAD,    Request.Method.HEAD);
        methodMap.put(RensouApi.Method.OPTIONS, Request.Method.OPTIONS);
        methodMap.put(RensouApi.Method.TRACE,   Request.Method.TRACE);
        methodMap.put(RensouApi.Method.PATCH,   Request.Method.PATCH);
    }

    private VolleyApiUtils() {}

    public static int getVolleyMethod(RensouApi api) {
        return methodMap.get(api.getMethod());
    }

    /**
     * JsonObjectRequest 生成。
     *
     * @param api
     * @param listener
     * @param errorListener
     * @return
     */
    public static JsonObjectRequest createJsonObjectRequest(RensouApi api,
                                                            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        Object requestBody = api.getRequestBody();
        if (requestBody == null) {
            return new JsonObjectRequest(getVolleyMethod(api), api.getUriString(),
                    listener, errorListener);
        } else if (requestBody instanceof String) {
            return new JsonObjectRequest(getVolleyMethod(api), api.getUriString(), (String) api.getRequestBody(),
                    listener, errorListener);
        } else if (requestBody instanceof JSONArray) {
            // JsonArray を送って JsonObject が返ってくるパターンは Volley にはないみたい。
            throw new AssertionError();
        } else if (requestBody instanceof JSONObject) {
            return new JsonObjectRequest(getVolleyMethod(api), api.getUriString(), (JSONObject) api.getRequestBody(),
                    listener, errorListener);
        } else {
            throw new AssertionError();
        }
    }

    /**
     * JsonArrayRequest 生成。
     *
     * @param api
     * @param listener
     * @param errorListener
     * @return
     */
    public static JsonArrayRequest createJsonArrayRequest(RensouApi api,
                                                          Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        // うーん、うまい方法ないかな。RensouApi のテンプレートパラメータがわかるといいんだけど…
        Object requestBody = api.getRequestBody();
        if (requestBody == null) {
            return new JsonArrayRequest(getVolleyMethod(api), api.getUriString(),
                    listener, errorListener);
        } else if (requestBody instanceof String) {
            return new JsonArrayRequest(getVolleyMethod(api), api.getUriString(), (String) api.getRequestBody(),
                    listener, errorListener);
        } else if (requestBody instanceof JSONArray) {
            return new JsonArrayRequest(getVolleyMethod(api), api.getUriString(), (JSONArray) api.getRequestBody(),
                    listener, errorListener);
        } else if (requestBody instanceof JSONObject) {
            return new JsonArrayRequest(getVolleyMethod(api), api.getUriString(), (JSONObject) api.getRequestBody(),
                    listener, errorListener);
        } else {
            // TODO: 開発中は落とせばいいんだけど。万が一、本番運用中に発生したときにもわかるようにしたい。
            throw new AssertionError();
        }
    }

    /**
     * StringRequest 生成。
     *
     * @param api
     * @param listener
     * @param errorListener
     * @return
     */
    public static StringRequest createStringRequest(RensouApi api,
                                                    Response.Listener<String> listener, Response.ErrorListener errorListener) {
        Object requestBody = api.getRequestBody();
        if (requestBody == null) {
            return new StringRequest(getVolleyMethod(api), api.getUriString(),
                                     listener, errorListener);
        } else {
            throw new AssertionError();
        }
    }

    // ↓ここらへん VolleyUtils に入れるかどうか悩む。

    /*
     * 送信処理の共通のパラメータ
     */
    private static int INITIAL_TIMEOUT_MS = 30000;  // 頑張りすぎかも
    private static DefaultRetryPolicy DEFAULT_RETRY_POLICY = new DefaultRetryPolicy(INITIAL_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    /**
     * 共通のパラメータで送信。
     */
    public static Request send(Context context, Request request) {
        request.setRetryPolicy(DEFAULT_RETRY_POLICY);
        return VolleyUtils.getRequestQueue(context).add(request);
    }

    // API によってはタイムアウト値を変えた方がいいかも
    public static Request send(Context context, Request request, int initialTimeoutMs) {
        DefaultRetryPolicy policy = new DefaultRetryPolicy(initialTimeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        return VolleyUtils.getRequestQueue(context).add(request);
    }


    public static void log(String tag, RensouApi api, JSONObject response) {
        Logger.i(tag, "HTTP: [" + api.getClass().getSimpleName() + "] response = " + response.toString());
    }

    public static void log(String tag, String name, JSONObject response) {
        Logger.i(tag, "HTTP: [" + name + "] response = " + response.toString());
    }

    public static void log(String tag, RensouApi api, JSONArray response) {
        Logger.i(tag, "HTTP: [" + api.getClass().getSimpleName() + "] response = " + response.toString());
    }

    public static void log(String tag, String name, JSONArray response) {
        Logger.i(tag, "HTTP: [" + name + "] response = " + response.toString());
    }

    public static void log(String tag, RensouApi api, VolleyError error) {
        Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] error.getMessage = " + error.getMessage());
        if (error.networkResponse != null) {
            Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] statusCode = " + error.networkResponse.statusCode);
            if (error.networkResponse.data != null) {
                String data = null;
                try {
                    data = new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(tag, "Data is not String.");
                }
                Logger.e(tag, "HTTP: [" + api.getClass().getSimpleName() + "] data = " + data);
            }
        }
    }

    public static void log(String tag, String name, VolleyError error) {
        Logger.e(tag, "HTTP: [" + name + "] error.getMessage = " + error.getMessage());
        if (error.networkResponse != null) {
            Logger.e(tag, "HTTP: [" + name + "] statusCode = " + error.networkResponse.statusCode);
            if (error.networkResponse.data != null) {
                String data = null;
                try {
                    data = new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Logger.e(tag, "Data is not String.");
                }
                Logger.e(tag, "HTTP: [" + name + "] data = " + data);
            }
        }
    }

    // API のエラーコードとメッセージ
    // TODO: Volley に依存しない部分は他の所に移動したい。
    public static class ApiError {
        public int code;
        public String message;

//        ApiError(int code, String message) {
//            this.code = code;
//            this.message = message;
//        }
    }

    public static ApiError parseVolleyError(VolleyError error) {
        if (error.networkResponse == null) {
            return null;
        }

        if (error.networkResponse.data == null) {
            return null;
        }

        ApiError apiError = new ApiError();
        try {
            String data = new String(error.networkResponse.data, "UTF-8");
            JSONObject o = new JSONObject(data);
            apiError.code = o.getInt("code");
            apiError.message = o.getString("message");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            // API でエラーを返さない場合にありうる
            e.printStackTrace();
            return null;
        }

        return apiError;
    }
}
