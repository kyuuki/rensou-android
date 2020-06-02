package jp.kyuuki.rensou.android.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import jp.kyuuki.rensou.android.Preferences;
import jp.kyuuki.rensou.android.components.VolleyApiUtils;
import jp.kyuuki.rensou.android.components.api.PutUserApi;
import jp.kyuuki.rensou.android.models.User;

public class RegistrationUtil {

    private static final String TAG = RegistrationUtil.class.getName();

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token, final Context context) {
        // ユーザー情報更新
        User user = User.getMyUser(context);
        final PutUserApi api = new PutUserApi(context, user.getId(), token);

        // Listener 使う時点で Volley 依存。
        Request postUserrequest = VolleyApiUtils.createJsonObjectRequest(api,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApiUtils.log(TAG, api, response);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, true).apply();

                        User user = api.parseResponseBody(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApiUtils.log(TAG, api, error);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, false).apply();
                        // TODO: 通信エラーの時はどうする？
                    }
                });

        VolleyApiUtils.send(context, postUserrequest);
    }
}
