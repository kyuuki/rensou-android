package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import jp.kyuuki.rensou.android.models.Rensou;

/**
 * いいね！ API。
 */
public class PostRensousLikeApi extends RensouApi<Void, Void, Void> {

    private long id;

    /*
     * リクエスト仕様
     */
    public PostRensousLikeApi(Context context, long id) {
        super(context);

        this.id = id;
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
        builder.path(getApiPathBase() + "/rensous/" + id + "/like");

        String lang = Locale.getDefault().getLanguage();
        builder.appendQueryParameter("lang", lang);

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
    public Void parseResponseBody(Void response) {
        return null;
    }
}
