package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.kyuuki.rensou.android.models.Rensou;

/**
 * 連想投稿 API。
 *
 * - リクエストの仕様 (送信に必要な情報、URL、リクエストボディ)。
 * - レスポンスの仕様 (返ってくる情報、レスポンスボディ)。
 * - モデルには依存しない方がよさげ。
 *   - JSON からモデルへの変換はここでやる。
 *   - JSON のデータ構造はモデルで意識しない。
 *   - API はモデルに依存してもいいけど、モデルは API に依存させない。
 */
public class PostRensouApi extends RensouApi<JSONObject, JSONArray, List<Rensou>> {

    private long userId;
    private long themeId;
    private String keyword;

    /*
     * リクエスト仕様
     */
    public PostRensouApi(Context context, long userId, long themeId, String keyword) {
        super(context);

        // パラメータは URL やリクエストボディに必要な情報。
        this.userId = userId;
        this.themeId = themeId;
        this.keyword = keyword;
    }

    @Override
    public Method getMethod() {
        return Method.POST;
    }

    @Override
    public String getUriString() {
        return getUri().toString();
    }

    private Uri getUri() {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme(getApiScheme());
        builder.encodedAuthority(getApiAuthority());
        builder.path(getApiPathBase() + "/rensou.json");

        builder.appendQueryParameter("app_id", this.appId);
        String lang = Locale.getDefault().getLanguage();
        builder.appendQueryParameter("lang", lang);

        return builder.build();
    }

    @Override
    public JSONObject getRequestBody() {
        JSONObject json = new JSONObject();

        try {
            json.put("theme_id", themeId);
            json.put("keyword", keyword);
            json.put("user_id", userId);
            json.put("room", RensouApi.ROOM_TYPE);
        } catch (JSONException e) {
            throw new AssertionError();
        }

        return json;
    }

    /*
     * レスポンス仕様
     */
    // この形にしちゃうと、API オブジェクトが見えるところじゃないと、使いづらい…
    // かといって、クラスメソッドにするとオーバーライドできない…
    // ここは共通の名前付けのルールぐらいで逃げるのが現実的か。
    @Override
    public List<Rensou> parseResponseBody(JSONArray response) {
        return json2Rensous(response);
    }

    /*
     * JSON データ構造
     */
    public static ArrayList<Rensou> json2Rensous(JSONArray a) {
        ArrayList<Rensou> list = new ArrayList<Rensou>();
        for (int i = 0, len = a.length(); i < len; i++) {
            try {
                JSONObject o = a.getJSONObject(i);

                Rensou r = GetRensouApi.json2Rensou(o);
                if (r != null) {
                    list.add(r);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        return list;
    }
}
