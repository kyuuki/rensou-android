package jp.kyuuki.rensou.android.models;

import java.util.HashSet;

import jp.kyuuki.rensou.android.Preference;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 自分のいいね！履歴。
 *
 * - 自分がいいね！した履歴をアプリ側で持っておく。
 */
public class MyLikes {
    HashSet<Long> history;  // いいね！した連想の ID を集合で保持

    SharedPreferences settings;  // 永続化のため、作成時に保存しておく
    
    private static MyLikes instance;  // メモリ不足の時に初期化されて null になる可能性あり
    
    private MyLikes(Context context) {
        this.settings = context.getSharedPreferences(Preference.NAME, Context.MODE_PRIVATE);
        
        String likesJson = this.settings.getString(Preference.KEY_MY_LIKES, null);
        if (likesJson == null) {
            likesJson = "[]";
        }

        JSONArray array;
        try {
            array = new JSONArray(likesJson);
        } catch (JSONException e) {
            e.printStackTrace();  // TODO: 出力するとこ決める
            array = new JSONArray(); 
        }

        this.history = new HashSet<Long>();
        for (int i = 0, len = array.length(); i < len; i++) {
            try {
                long id = array.getLong(i);
                this.history.add(id);
            } catch (JSONException e) {
                e.printStackTrace();  // TODO: 出力するとこ決める
                // 変な値が入っていても、とりあえず無視。
            }
        }
    }

    public static MyLikes getInstance(Context context) {
        if (instance == null) {
            instance = new MyLikes(context);
        }

        return instance;
    }
    
    public boolean isLike(long rensouId) {
        return history.contains(rensouId);
    }
    
    public void like(long rensouId) {
        history.add(rensouId);
        saveMyLikes();
    }
    
    public void unlike(long rensouId) {
        history.remove(rensouId);
        saveMyLikes();
    }
    
    private void saveMyLikes() {
        JSONArray array = new JSONArray(this.history);
        
        SharedPreferences.Editor edit = this.settings.edit();
        edit.putString(Preference.KEY_MY_LIKES, array.toString());
        edit.commit();
    }
}
