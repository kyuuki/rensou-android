package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.kyuuki.rensou.android.models.Rank;
import jp.kyuuki.rensou.android.models.Rensou;

/**
 * ランキング取得 API。
 */
public class GetRensousRankingApi extends RensouApi<Void, JSONArray, List<Rank>> {

    /*
     * リクエスト仕様
     */
    public GetRensousRankingApi(Context context) {
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
        builder.path(getApiPathBase() + "/rensous/ranking");

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
    public List<Rank> parseResponseBody(JSONArray response) {
        return json2Ranking(response);
    }

    /*
     * JSON 仕様
     */
    public static ArrayList<Rank> json2Ranking(JSONArray a) {
        ArrayList<Rank> list = new ArrayList<Rank>();
        for (int i = 0, len = a.length(); i < len; i++) {
            try {
                JSONObject o = a.getJSONObject(i);

                Rensou r = GetRensouApi.json2Rensou(o);
                Rank rank = new Rank(i + 1, r);
                list.add(rank);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        return list;
    }
}

