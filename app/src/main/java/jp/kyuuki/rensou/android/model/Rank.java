package jp.kyuuki.rensou.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.net.RensouApi;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

/**
 * ランク。
 */
public class Rank implements Serializable {
    private static final long serialVersionUID = -6029400733997760777L;

    private int rank;
    private Rensou rensou;

    public Rank(int rank, Rensou rensou) {
        this.rank = rank;
        this.rensou = rensou;
    }
    
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Rensou getRensou() {
        return rensou;
    }

    public void setRensou(Rensou rensou) {
        this.rensou = rensou;
    }

    // 以下、ダミー用

    public static List<Rank> createDummyRanking(Resources resources) throws JSONException, IOException {
        // JSON 読み込み
        InputStream is = resources.openRawResource(R.raw.dummy_ranking);
        JSONArray array = new JSONArray(IOUtils.toString(is));

        List<Rank> ranking = new ArrayList<Rank>();
        
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            
            Rensou r = new Rensou();
            r.setOldKeyword(o.getString("old_keyword"));
            r.setKeyword(o.getString("keyword"));
            r.setCreatedAt(RensouApi.parseDate(o.getString("created_at")));
            r.setFavorite(o.getInt("favorite"));
            Rank rank = new Rank(i + 1, r);
            ranking.add(rank);
        }
        
        return ranking;
    }
}
