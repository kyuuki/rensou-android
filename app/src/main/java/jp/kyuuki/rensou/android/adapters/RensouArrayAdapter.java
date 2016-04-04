package jp.kyuuki.rensou.android.adapters;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.commons.RensouUtils;
import jp.kyuuki.rensou.android.components.VolleyApi;
import jp.kyuuki.rensou.android.models.MyLikes;
import jp.kyuuki.rensou.android.models.Rensou;
import jp.kyuuki.rensou.android.components.api.RensouApi;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RensouArrayAdapter extends ArrayAdapter<Rensou> {
    LayoutInflater mInflater;
    
    // 日付のフォーマット → 多言語化により Andorid 標準の表示形式にあわせることにした。
    //@SuppressLint("SimpleDateFormat")
    //static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public RensouArrayAdapter(Context context, int textViewResourceId, List<Rensou> list) {
        super(context, textViewResourceId, list);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    // レイアウトファイルに記述してある値を 1 度だけ取得する。取得後は正になるはず。
    static int rowPaddingLeft   = -1;
    static int rowPaddingTop    = -1;
    static int rowPaddingRight  = -1;
    static int rowPaddingBottom = -1;
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.row_rensou, null);
            holder = new ViewHolder();
            holder.rowRensouLayout   = (RelativeLayout) view.findViewById(R.id.rowRensouLayout);
            holder.dateTimeText      = (TextView) view.findViewById(R.id.dateTimeText);
            holder.rensouText        = (TextView) view.findViewById(R.id.rensouText);
            holder.spamImage         = (ImageView) view.findViewById(R.id.spamImage);
            holder.likeImage         = (ImageView) view.findViewById(R.id.likeImage);
            holder.favoriteCountText = (TextView) view.findViewById(R.id.favoriteCountText);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        
        final Rensou rensou = getItem(position);
        holder.rensouId = rensou.getId();
        holder.spamImage.setVisibility(View.INVISIBLE);  // しばらく、通報機能は無効に。
        
        // View
        
        // 各種表示データ更新
        updateRensouView(getContext(), holder, rensou);
        
        // 1 行おきに背景を変える
//        int left   = rowPaddingLeft   > 0 ? rowPaddingLeft   : rowRensouLayout.getPaddingLeft();
//        int top    = rowPaddingTop    > 0 ? rowPaddingTop    : rowRensouLayout.getPaddingTop();
//        int right  = rowPaddingRight  > 0 ? rowPaddingRight  : rowRensouLayout.getPaddingRight();
//        int bottom = rowPaddingBottom > 0 ? rowPaddingBottom : rowRensouLayout.getPaddingBottom();
        // TODO: ↑なぜか値が一定しない。

        float density = getContext().getResources().getDisplayMetrics().density;
        int left   = (int) (density * 30);
        int top    = (int) (density * 10);
        int right  = (int) (density * 40);
        int bottom = (int) (density * 15);
        if (position % 2 == 0) {
            holder.rowRensouLayout.setBackgroundResource(R.drawable.rensou_cell_bg);
            holder.rowRensouLayout.setPadding(left, top, right, bottom);
        } else {
            holder.rowRensouLayout.setBackgroundResource(R.drawable.rensou_cell_bg2);
            holder.rowRensouLayout.setPadding(left + (int) (density * 10), top, right
                    - (int) (density * 10), bottom);  // 上の画像が左に吹き出しが付くので通常より右に移動
        }
        
        return view;
    }
    
    // 吹き出し以外の表示内容を更新する。ボタンのイベントハンドラも
    // - オブジェクトで使っているのは mRequestQueue だけ
    private void updateRensouView(final Context context, final ViewHolder holder, final Rensou rensou) {
        // 連想結果表示テキスト
        holder.rensouText.setText(Html.fromHtml(RensouUtils.rensouToHtml(rensou, context)));

        // 投稿日付
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);  // TODO: 効率化
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);  // TODO: 効率化
        if (rensou.getCreatedAt() != null) {
            //dateTimeText.setText(sdf.format(rensouHistory.getRensou().getCreatedAt()));
            Date d = rensou.getCreatedAt();
            holder.dateTimeText.setText(dateFormat.format(d) + " " + timeFormat.format(d));
        }

        // いいね！状態
        MyLikes likes = MyLikes.getInstance(context);
        if (likes.isLike(rensou.getId())) {
            holder.likeImage.setImageResource(R.drawable.button_like_on);
        } else {
            holder.likeImage.setImageResource(R.drawable.button_like_off);
        }

        // いいね！数
        holder.favoriteCountText.setText(context.getString(R.string.list_rensou_like_count, rensou.getFavorite()));
        
        // いいね！ボタン
        holder.likeImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 連打対策
                v.setOnClickListener(null);
                
                MyLikes likes = MyLikes.getInstance(context);
                if (likes.isLike(rensou.getId())) {
                    // いいね！取り消し
                    String url = RensouApi.getPostUrlRensousLike(rensou.getId());
                    RensousLikeListener listener = new RensousLikeListener(rensou, holder, false);
                    StringRequest request = new StringRequest(Method.DELETE, url, listener, listener);
                    VolleyApi.send(getContext(), request);
                } else {
                    // いいね！
                    String url = RensouApi.getDeleteUrlRensousLike(rensou.getId());
                    RensousLikeListener listener = new RensousLikeListener(rensou, holder, true);
                    StringRequest request = new StringRequest(Method.POST, url, listener, listener);
                    VolleyApi.send(getContext(), request);
                }
            }
        });
    }

    // ViewHolder パターン
    private static class ViewHolder {
        RelativeLayout rowRensouLayout;
        TextView dateTimeText ;
        TextView rensouText;
        ImageView spamImage;
        ImageView likeImage;
        TextView favoriteCountText;
        
        long rensouId;  // 表示している連想 ID もここに保持
    }
    
    /*
     * POST or DELETE rensous/:rensou_id/like 受信後の動作。
     * 
     * mInflater を使っているので static のないインナークラス
     */
    private class RensousLikeListener implements Listener<String>, ErrorListener {
        Rensou rensou;      // いいね！した連想
        ViewHolder holder;  // 押された行の ViewHolder
        boolean isLike;     // true: いいね！ / false: いいね！取り消し

        RensousLikeListener(Rensou rensou, ViewHolder holder, boolean isLike) {
            this.rensou = rensou;
            this.holder = holder;
            this.isLike = isLike;
        }

        @Override
        public void onResponse(String response) {
            Logger.v("HTTP", "body is " + response);
            
            Context context = mInflater.getContext();
            
            /*
             * 内部状態更新
             */
            // リスト表示の元ネタのモデル
            if (isLike) {
                rensou.setFavorite(rensou.getFavorite() + 1);
            } else {
                rensou.setFavorite(rensou.getFavorite() - 1);
            }

            // いいね！履歴
            MyLikes likes = MyLikes.getInstance(context);  // TODO: 将来的には連想モデルに含めた方がいいかも
            if (isLike) {
                likes.like(rensou.getId());
            } else {
                likes.unlike(rensou.getId());
            }

            /*
             * 表示更新
             */
            if (holder.rensouId == rensou.getId()) {  // スクロールしてしまって、別連想を表示していないかチェック
                updateRensouView(context, holder, rensou);
            }

            // いいね！のときだけトースト表示 (取り消しは表示しない)
            if (isLike) {
                Toast.makeText(context, context.getString(R.string.list_rensou_like), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Logger.v("HTTP", "error is " + error);
            Toast.makeText(mInflater.getContext(), mInflater.getContext().getString(R.string.error_communication), Toast.LENGTH_LONG).show();

            /*
             * 表示更新 (エラーでもいいね！ボタンだけは復活させる)
             */
            if (holder.rensouId == rensou.getId()) {  // スクロールしてしまって、別連想を表示していないかチェック
                updateRensouView(mInflater.getContext(), holder, rensou);
            }
        }
    }
}
