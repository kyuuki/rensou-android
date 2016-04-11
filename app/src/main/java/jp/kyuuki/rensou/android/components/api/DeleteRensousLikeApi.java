package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

/**
 * いいね！取り消し API。
 */
public class DeleteRensousLikeApi extends RensouApi<Void, Void, Void> {

    private long id;

    /*
     * リクエスト仕様
     */
    public DeleteRensousLikeApi(Context context, long id) {
        super(context);

        this.id = id;
    }

    @Override
    public Method getMethod() {
        return Method.DELETE;
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
