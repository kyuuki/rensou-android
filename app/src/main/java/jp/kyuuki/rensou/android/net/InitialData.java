package jp.kyuuki.rensou.android.net;

import org.json.JSONObject;

/**
 * 初期データ。
 * 
 * - 1.1.0 から導入した初期データをネットワークから取得するしくみ。
 * - 入口はできるだけ、シンプルなしくみにしておく。
 * - バージョン間の互換性をここに記述。
 */
public class InitialData {

    private static String KEY_MESAGE = "message";
    private static String KEY_API_BASE_URL = "api_base_url";

    private String messege;     // メッセージがあったら出力する。何も考えずに毎回出力する (> 1.1.0)
    private String apiBaseUrl;  // API ベース URL を変更。トラブルがあった時用。 (> 1.1.0)

    private InitialData() {}

    // 作成できない (null が返る) 場合が可能性があるので注意  (現状ない)
    public static InitialData createInitialData(JSONObject o) {
        InitialData data = new InitialData();
        data.setMessege(o.optString(KEY_MESAGE, null));
        data.setApiBaseUrl(o.optString(KEY_API_BASE_URL, null));
        return data;
    }

    /* ------------- *
     * Setter/Getter *
     * ------------- */

    public String getMessege() {
        return messege;
    }

    public void setMessege(String messege) {
        this.messege = messege;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
}
