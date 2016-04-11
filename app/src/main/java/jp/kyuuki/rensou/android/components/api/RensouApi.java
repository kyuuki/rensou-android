package jp.kyuuki.rensou.android.components.api;

import android.content.Context;
import android.net.Uri;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.components.InitialData;

/**
 * 連想 API。
 * 
 * - API 仕様にかかわる部分をここに集約。
 * - 通信ライブラリには依存しない。
 *
 * @param <T1>  リクエストボディの型
 * @param <T2>  レスポンスボディの型
 * @param <T3>  レスポンスボディから変換されるモデルの型
 */
abstract public class RensouApi<T1, T2, T3> {
    private static final String TAG = RensouApi.class.getName();

    public enum Method { GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH }

    // iOS 版サーバーの部屋対応 (1: 学生ルーム, 2: 社会人ルーム, 3: ガールズルーム, 4: おたくルーム, 5: 秘密の部屋)
    protected final static int ROOM_TYPE = 3;

    RensouApi(Context context) {
        if (cacheApiScheme == null || cacheApiAuthority == null || cacheApiPathBase == null) {
            setApiBase(context);
        }
    }

    abstract public Method getMethod();
    abstract public String getUriString();
    abstract public T1 getRequestBody();
    abstract public T3 parseResponseBody(T2 json);

    /*
     * API ベース URL 関連
     */
    // これらの変数が初期化される不安に常にさいなまれる
    protected static String cacheApiScheme = null;
    protected static String cacheApiAuthority = null;
    protected static String cacheApiPathBase = null;

    // これをそんなにやらなくて済むように。
    protected static void setApiBase(Context context) {
        // API URL は初期データにあればそれを、なければ config.xml の値を利用する。
        String api_base_url = InitialData.getApiBaseUrl(context);
        if (api_base_url == null) {
            api_base_url = context.getString(R.string.api_uri_base);
        }
        Uri uri = Uri.parse(api_base_url);

        cacheApiScheme = uri.getScheme();
        cacheApiAuthority = uri.getEncodedAuthority();
        cacheApiPathBase = uri.getPath();
    }

    // 以下の Getter が呼び出される前に setApiBase() が呼び出されていること。
    protected static String getApiScheme() {
        return cacheApiScheme;
    }

    protected static String getApiAuthority() {
        return cacheApiAuthority;
    }

    protected static String getApiPathBase() {
        return cacheApiPathBase;
    }

    /*
     * 日付関係
     */
    // http://www.adakoda.com/adakoda/2010/02/android-iso-8601-parse.html
    static FastDateFormat fastDateFormat1 = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;  // yyyy-MM-dd'T'HH:mm:ssZZ

    // 2010-02-27T13:00:00Z がパースできない。 2010-02-27T13:00:00+00:00 と同義っぽいんだけど。
    // http://stackoverflow.com/questions/424522/how-can-i-recognize-the-zulu-time-zone-in-java-dateutils-parsedate
    static FastDateFormat fastDateFormat2 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // iOS 版サーバーからミリ秒がやってくるようになったのに対応
    static FastDateFormat fastDateFormat3 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static String patterns[] = { fastDateFormat1.getPattern(), fastDateFormat2.getPattern(), fastDateFormat3.getPattern() };

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