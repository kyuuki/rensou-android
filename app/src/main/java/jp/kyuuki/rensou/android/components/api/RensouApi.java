package jp.kyuuki.rensou.android.components.api;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
//import org.apache.http.impl.cookie.DateParseException;
//import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jp.kyuuki.rensou.android.Config;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.models.Rank;
import jp.kyuuki.rensou.android.models.Rensou;
import jp.kyuuki.rensou.android.models.User;

/**
 * 連想 API。
 * 
 * - API 仕様にかかわる部分をここに集約。
 * - 通信ライブラリには依存したくない。
 */
public class RensouApi {
    private static final String TAG = RensouApi.class.getName();

    // 書き換えられることがあるけど、ほぼ定数のイメージなので大文字にしておく。
    public static String BASE_URL = Config.API_BASE_URL;
    
    // http://www.adakoda.com/adakoda/2010/02/android-iso-8601-parse.html
    static FastDateFormat fastDateFormat1 = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;  // yyyy-MM-dd'T'HH:mm:ssZZ
    
    // 2010-02-27T13:00:00Z がパースできない。 2010-02-27T13:00:00+00:00 と同義っぽいんだけど。
    // http://stackoverflow.com/questions/424522/how-can-i-recognize-the-zulu-time-zone-in-java-dateutils-parsedate
    static FastDateFormat fastDateFormat2 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // iOS 版サーバーからミリ秒がやってくるようになったのに対応
    static FastDateFormat fastDateFormat3 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static String patterns[] = { fastDateFormat1.getPattern(), fastDateFormat2.getPattern(), fastDateFormat3.getPattern() };

    // iOS 版サーバーの部屋対応 (1: 学生ルーム, 2: 社会人ルーム, 3: ガールズルーム, 4: おたくルーム, 5: 秘密の部屋)
    private final static int ROOM_TYPE = 3;

    /*
     * API JSON 仕様
     */
    
    // POST user
    public static String getPostUrlRegisterUser() {
        return BASE_URL + "/user";
    }

    public static JSONObject makeRegisterUserJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("device_type", 1);
        } catch (JSONException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            return null;
        }

        return json;
    }
    
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

    // GET rensou.json
    public static String getGetUrlLast() {
        return BASE_URL + "/rensou.json?room=" + ROOM_TYPE;
    }

    // POST rensou.json
    public static String getPostUrl() {
        return BASE_URL + "/rensou.json";
    }

    public static JSONObject makeRensouJson(long themeId, String keyword, User user) {
        JSONObject json = new JSONObject();
        try {
            json.put("theme_id", themeId);
            json.put("keyword", keyword);
            json.put("user_id", user.getId());
            json.put("room", ROOM_TYPE);
        } catch (JSONException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            return null;
        }
        
        return json;
    }

    // POST rensous/:rensou_id/like
    public static String getPostUrlRensousLike(long id) {
        return BASE_URL + "/rensous/" + id + "/like";
    }
    
    // DELETE rensous/:rensou_id/like
    public static String getDeleteUrlRensousLike(long id) {
        return BASE_URL + "/rensous/" + id + "/like";
    }

    // GET rensous/ranking
    public static String getGetUrlRensousRanking() {
        return BASE_URL + "/rensous/ranking?room=" + ROOM_TYPE;
    }
    
    // 現状、レスポンスは連想のリスト
    public static ArrayList<Rank> json2Ranking(JSONArray a) {
        ArrayList<Rank> list = new ArrayList<Rank>();
        for (int i = 0, len = a.length(); i < len; i++) {
            try {
                JSONObject o = a.getJSONObject(i);
                
                Rensou r = RensouApi.json2Rensou(o);
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
    
    /*
     * API 共通モデル
     */

    public static ArrayList<Rensou> json2Rensous(JSONArray a) {
        ArrayList<Rensou> list = new ArrayList<Rensou>();
        for (int i = 0, len = a.length(); i < len; i++) {
            try {
                JSONObject o = a.getJSONObject(i);
                
                Rensou r = RensouApi.json2Rensou(o);
                list.add(r);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
        
        return list;
    }

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
    
    // API 仕様変更されてもいいように、それなりの値を返してしまう。ただ、エラーはどこかで検知したい。
    public static Date parseDate(String s) {
        Date d;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("JST")); // TODO: 日本依存

        try {
            d = dateFormatter.parse(s);
        } catch (ParseException e) {
//            d = DateUtils.parseDate(s, patterns);
//        } catch (DateParseException e) {
            Logger.w(TAG, "parseDate error " + s);
            d = null;
        }
            return d;
    }
}