package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import jp.kyuuki.rensou.android.models.User;

/**
 * ユーザー更新 API。
 */
public class PutUserApi extends RensouApi<JSONObject, JSONObject, User> {

    private long userId;
    private String regId;

    /*
     * リクエスト仕様
     */
    public PutUserApi(Context context, long userId, String regId) {
        super(context);

        this.userId = userId;
        this.regId = regId;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public String getUriString() {
        return getUri().toString();
    }

    private Uri getUri() {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme(getApiScheme());
        builder.encodedAuthority(getApiAuthority());
        builder.path(getApiPathBase() + "/user");

        builder.appendQueryParameter("app_id", this.appId);

        return builder.build();
    }

    @Override
    public JSONObject getRequestBody() {
        JSONObject json = new JSONObject();

        try {
            json.put("user_id", userId);
            json.put("registration_token", regId);
        } catch (JSONException e) {
            throw new AssertionError();
        }

        return json;
    }

    /*
     * レスポンス仕様
     */
    @Override
    public User parseResponseBody(JSONObject response) {
        return json2User(response);
    }

    /*
     * JSON 仕様
     */
    public static User json2User(JSONObject o) {
        User user;
        try {
            user = new User(o.getLong("user_id"));
        } catch (JSONException e) {
            // TODO: JSON 構文解析エラー処理
            e.printStackTrace();
            return null;
        }

        return user;
    }
}
