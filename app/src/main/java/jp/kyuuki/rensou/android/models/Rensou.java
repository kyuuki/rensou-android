package jp.kyuuki.rensou.android.models;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Utils;
import jp.kyuuki.rensou.android.components.api.RensouApi;

/**
 * 連想。
 */
public class Rensou implements Serializable {
    private static final long serialVersionUID = 3403855697655211867L;

    private long id;
    private long userId;
    private String oldKeyword;
    private String keyword;
    private int favorite;
    private boolean spam;
    private Date createdAt;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getOldKeyword() {
        return oldKeyword;
    }
    public void setOldKeyword(String oldKeyword) {
        this.oldKeyword = oldKeyword;
    }
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public int getFavorite() {
        return favorite;
    }
    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
    public boolean isSpam() {
        return spam;
    }
    public void setSpam(boolean spam) {
        this.spam = spam;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    // 入力チェック
    // null 不可
    public static boolean validateKeyword(String keyword) {
        if (Utils.isForbiddenOnlySpace(keyword)) {
            return false;
        }
        
        int length = keyword.length();
        if (length == 0 || length > 13) {
            return false;
        }
        
        return true;
    }
    
    // 以下、ダミー用

    // 投稿できるように ID は本物を使用する。
    public static Rensou createDummyRensou(Resources resources, long id) {
        // 連想リストの一番上を使う
        ArrayList<Rensou> list = null;
        try {
            list = createDummyRensouList(resources);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Rensou r = list.get(0);
        r.setId(id);
        
        return r;
    }
    
    public static ArrayList<Rensou> createDummyRensouList(Resources resources) throws JSONException, IOException {
        ArrayList<Rensou> list = new ArrayList<Rensou>();
        
        InputStream is = resources.openRawResource(R.raw.dummy_rensou_list);
        JSONArray array = new JSONArray(IOUtils.toString(is));

        String oldKeyword = "";
        
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);

            Rensou r = new Rensou();
            r.setOldKeyword(oldKeyword);
            r.setKeyword(o.getString("keyword"));
            r.setCreatedAt(RensouApi.parseDate(o.getString("created_at")));
            r.setFavorite(o.getInt("favorite"));

            oldKeyword = o.getString("keyword");

            list.add(0, r);  // 最初に入れる
        }
        
        return list;
    }
}
