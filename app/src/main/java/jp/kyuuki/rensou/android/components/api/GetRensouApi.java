package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

import jp.kyuuki.rensou.android.models.Rensou;

/**
 * 連想取得 API。
 */
public class GetRensouApi extends RensouApi<Void, JSONObject, Rensou> {

    /*
     * リクエスト仕様
     */
    public GetRensouApi(Context context) {
        super(context);
    }

    @Override
    public Method getMethod() {
        return Method.GET;
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
        builder.appendQueryParameter("room", String.valueOf(ROOM_TYPE));

        return builder.build();
    }

    @Override
    public Void getRequestBody() {
        return null;
    }

    /*
     * レスポンス仕様
     */
    @Override
    public Rensou parseResponseBody(JSONObject response) {
        return json2Rensou(response);
    }

    /*
     * JSON データ構造
     */
    public static Rensou json2Rensou(JSONObject o) {
        Rensou rensou = new Rensou();
        try {
            rensou.setId(o.getLong("id"));
            rensou.setUserId(o.getLong("user_id"));
            rensou.setOldKeyword(o.getString("old_keyword"));
            rensou.setKeyword(o.getString("keyword"));
            rensou.setFavorite(o.getInt("favorite"));
            Date d = parseDate(o.getString("created_at"));
            rensou.setCreatedAt(d);
        } catch (JSONException e) {
            // TODO: JSON 構文解析エラー処理
            // TODO: 致命的なエラーをイベント送信するしくみ
            e.printStackTrace();
            return null;
        }

        return rensou;
    }
}
