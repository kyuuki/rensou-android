package jp.kyuuki.rensou.android.fragments;

import java.util.ArrayList;
import java.util.Locale;

import jp.kyuuki.rensou.android.Analysis;
import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.activities.PostResultActivity;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.commons.VolleyUtils;
import jp.kyuuki.rensou.android.components.VolleyApi;
import jp.kyuuki.rensou.android.models.Rensou;
import jp.kyuuki.rensou.android.models.User;
import jp.kyuuki.rensou.android.components.api.RensouApi;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * 連想投稿フラグメント。
 */
public class PostRensouFragment extends Fragment {
    // Model
    private long mThemeId = -1;

    // View
    private TextView mLatestKeywordText;
    private EditText mPostRensouEditText;
    private Button mAnswerButton;

    ProgressDialog progressDialog;  // 通信中ダイアログ

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 多言語化処理
        View v;
        if (Locale.JAPAN.equals(Locale.getDefault())) {
            v = inflater.inflate(R.layout.fragment_post_rensou, container, false);
        } else {
            // 日本語以外のリソースを用意していないので、一部クラシックスタイルで表示する。
            v = inflater.inflate(R.layout.fragment_post_rensou_noja, container, false);
        }

        mLatestKeywordText = (TextView) v.findViewById(R.id.latestKeywordText);
        mPostRensouEditText = (EditText) v.findViewById(R.id.postRensouEditText);
        mAnswerButton = (Button) v.findViewById(R.id.postButton);
        mAnswerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().sendEvent(Analysis.GA_EC_UI_ACTION, Analysis.GA_EA_BUTTON_POST, null, null);
                
                // 入力チェック
                String keyword = mPostRensouEditText.getText().toString();
                if (Rensou.validateKeyword(keyword) == false) {
                    EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_VALIDATE, null, null);
                    Toast.makeText(getActivity(), getString(R.string.post_rensou_error_validate_keyword), Toast.LENGTH_LONG).show();
                    return;
                }
                
                String url = RensouApi.getPostUrl();
                JSONObject json = RensouApi.makeRensouJson(mThemeId, keyword, User.getMyUser(getActivity()));
                
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getString(R.string.post_rensou_posting));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                JsonArrayRequest request = new JsonArrayRequest(Method.POST, url, json,
                    new Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // タイミングによってはダイアログがない場合があるかも
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            progressDialog = null;

                            Logger.v("HTTP", "body is " + response);

                            ArrayList<Rensou> list = RensouApi.json2Rensous(response);
                            
                            // DUMMY
                            //try { list = Rensou.createDummyRensouList(getResources()); } catch (Exception e) {}

                            Intent intent = new Intent(getActivity(), PostResultActivity.class);
                            intent.putExtra("list", list);
                            startActivity(intent);
                        }
                    },

                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // タイミングによってはダイアログがない場合があるかも
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            progressDialog = null;
                            
                            if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                                // 投稿が被った可能性が高い。
                                EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_CONFLICT, null, null);
                                Toast.makeText(getActivity(), getString(R.string.post_rensou_error_transaction), Toast.LENGTH_LONG).show();
                                mPostRensouEditText.setText("");
                                getLatestRensou();
                            } else {
                                EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_ERROR, null, null);
                                Toast.makeText(getActivity(), getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                );

                VolleyApi.send(getActivity().getApplicationContext(), request);
            }
        });
        
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();

        // 前回入力した投稿削除
        mPostRensouEditText.setText("");

        // 最新の連想取得
        getLatestRensou();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Fragment のページビューを取ると Activity と混乱するので、このアプリでは取らない。
//        EasyTracker.getInstance().setContext(getActivity());
//        EasyTracker.getTracker().sendView("PostRensouFragment");
    }
    
    // 最後の連想取得
    private void getLatestRensou() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.post_rensou_reading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        String url = RensouApi.getGetUrlLast();
        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // タイミングによってはダイアログがない場合があるようだ (1 回ヌルポで落ちた)
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = null;

                    Logger.e("HTTP", "body is " + response.toString());
                    Rensou rensou = RensouApi.json2Rensou(response);

                    // DUMMY
                    //rensou = Rensou.createDummyRensou(getResources(), rensou.getId());

                    // TODO: mLastKeywordText が作成出来ていないパターンがある？
                    mThemeId = rensou.getId();
                    mLatestKeywordText.setText(rensou.getKeyword());
                }
            },

            new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // タイミングによってはダイアログがない場合があるかも
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = null;

                    EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_GET_LAST_KEYWORD, null, null);
                    Toast.makeText(getActivity(), getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                    // TODO: 通信エラーの時はどうする？
                }
            });

        VolleyApi.send(getActivity().getApplicationContext(), request);
    }
}
