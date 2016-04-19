package jp.kyuuki.rensou.android.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.kyuuki.rensou.android.Preferences;
import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;

/**
 * 初期データ。
 *
 * - 1.1.0 から導入した初期データをネットワークから取得するしくみ。
 * - API が死んでいても動くように。
 * - API より信頼性が高いしくみを使う。
 * - 入口なので、できるだけシンプルなしくみに。
 * - バージョン間の互換性をここに記述。
 */
public class InitialData {
    static final String TAG = InitialData.class.getName();

    private static String KEY_MESSAGE = "message";
    private static String KEY_API_BASE_URL = "api_base_url";

    private String message;     // メッセージがあったら出力する。何も考えずに毎回出力する (> 1.1.0)
    private String apiBaseUrl;  // API ベース URL を変更。トラブルがあった時用。 (> 1.1.0)

    private InitialData() {}

    /**
     * ファクトリーメソッド。
     *
     * - 作成できない (null が返る) 場合が可能性があるので注意  (現状ない)
     */
    public static InitialData createInitialData(JSONObject o) {
        InitialData data = new InitialData();

        data.setMessage(o.optString(KEY_MESSAGE, null));
        data.setApiBaseUrl(o.optString(KEY_API_BASE_URL, null));

        return data;
    }


    /* ------------- *
     * Setter/Getter *
     * ------------- */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }


    /* ------ *
     * 永続化 *
     * ------ */

    private static void save(Context context, InitialData initialData) {
        SharedPreferences pref = context.getSharedPreferences(Preferences.NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(Preferences.KEY_MESSAGE, initialData.getMessage());
        edit.putString(Preferences.KEY_API_BASE_URL, initialData.getApiBaseUrl());
        edit.commit();
    }

    private static void clear(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Preferences.NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(Preferences.KEY_MESSAGE);
        edit.remove(Preferences.KEY_API_BASE_URL);
        edit.commit();
    }

    // オブジェクトには関係ないところで API ベース URL は取得する
    public static String getApiBaseUrl(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Preferences.NAME, Context.MODE_PRIVATE);
        String apiBaseUrl = pref.getString(Preferences.KEY_API_BASE_URL, null);

        return apiBaseUrl;
    }


    /* -------- *
     * 通信関連 *
     * -------- */
    // 完全に Volley 依存

    // 通信中は非 null
    private static Request request = null;

    // コールバック対象
    private static List<Callback> callbackList = new ArrayList<>();

    /**
     * 初期データ取得。
     *
     * - 戻り値が null の場合はコールバックにて、取得完了が通知される。
     */
    public static InitialData getInitialData(final Context context, final Callback callback) {
        // シンプルなしくみにするために、初期データは毎回することにする。
        // よって、初期データのサイズには気を付けること。

        if (request != null) {
            Logger.v(TAG, "Add callback");
            callbackList.add(callback);
            return null;
        }

        String url = context.getString(R.string.initial_data_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApiUtils.log(TAG, "initalDataGet", response);
                        InitialData.request = null;

                        InitialData initialData = InitialData.createInitialData(response);

                        if (initialData != null) {
                            InitialData.save(context, initialData);
                            allCallbackOnSuccessGetInitialData(initialData);
                        } else {
                            InitialData.clear(context);
                            allCallbackOnErrorGetInitialData();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApiUtils.log(TAG, "initalDataGet", error);
                        InitialData.request = null;

                        InitialData.clear(context);

                        // コールバック
                        allCallbackOnErrorGetInitialData();
                    }
                });

        // 送信処理
        InitialData.request = VolleyApiUtils.send(context, request);
        InitialData.callbackList.add(callback);

        return null;
    }

    public static void cancelInitialData(Callback callback) {
        Logger.v(TAG, "Cancel callback");
        callbackList.remove(callback);
        // 待っている人が誰もいなくなったら、リクエスト自体もキャンセル
        if (callbackList.size() == 0) {
            if (request != null) {  // 無条件でこのメソッド呼んでいいのでリクエスト中じゃないこともある
                Logger.v(TAG, "request.cancel()");
                request.cancel();
                request = null;
            }
        }
    }

    private static void allCallbackOnSuccessGetInitialData(InitialData initialData) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            Logger.v(TAG, "Callback success");
            callback.onSuccessGetInitialData(initialData);
            it.remove();
        }
    }

    private static void allCallbackOnErrorGetInitialData() {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            Logger.v(TAG, "Callback error");
            callback.onErrorGetInitialData();
            it.remove();
        }
    }

    // コールバック
    public static interface Callback {
        public void onSuccessGetInitialData(InitialData initialData);
        public void onErrorGetInitialData();
    }
}