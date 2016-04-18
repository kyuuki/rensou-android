package jp.kyuuki.rensou.android.fragments;

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

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.activities.PostResultActivity;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.components.VolleyApiUtils;
import jp.kyuuki.rensou.android.components.api.GetRensouApi;
import jp.kyuuki.rensou.android.components.api.PostRensouApi;
import jp.kyuuki.rensou.android.models.Rensou;
import jp.kyuuki.rensou.android.models.User;

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
                //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_UI_ACTION, Analysis.GA_EA_BUTTON_POST, null, null);
                
                // 入力チェック
                String keyword = mPostRensouEditText.getText().toString();
                if (Rensou.validateKeyword(keyword) == false) {
                    //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_VALIDATE, null, null);
                    Toast.makeText(getActivity(), getString(R.string.post_rensou_error_validate_keyword), Toast.LENGTH_LONG).show();
                    return;
                }
                
                final PostRensouApi api = new PostRensouApi(getContext(), User.getMyUser(getContext()).getId(), mThemeId, keyword);

                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage(getString(R.string.post_rensou_posting));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                JsonArrayRequest request = VolleyApiUtils.createJsonArrayRequest(api,
                        new Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // タイミングによってはダイアログがない場合があるかも
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                progressDialog = null;

                                Logger.v("HTTP", "body is " + response);

                                // TODO ArrayList 依存になってしまうけど、putExtra でやりとりするにはしたかがない？
                                //List<Rensou> list = api.parseResponseBody(response);
                                ArrayList<Rensou> list = (ArrayList<Rensou>) api.parseResponseBody(response);

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
                                    //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_CONFLICT, null, null);
                                    Toast.makeText(getActivity(), getString(R.string.post_rensou_error_transaction), Toast.LENGTH_LONG).show();
                                    mPostRensouEditText.setText("");
                                    getLatestRensou();
                                } else {
                                    //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_POST_ERROR, null, null);
                                    Toast.makeText(getActivity(), getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );

                VolleyApiUtils.send(getActivity().getApplicationContext(), request);
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

        final GetRensouApi api = new GetRensouApi(getContext());
        JsonObjectRequest request = VolleyApiUtils.createJsonObjectRequest(api,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // タイミングによってはダイアログがない場合があるようだ (1 回ヌルポで落ちた)
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        progressDialog = null;

                        Logger.e("HTTP", "body is " + response.toString());
                        Rensou rensou = api.parseResponseBody(response);

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

                        //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_ERROR, Analysis.GA_EA_GET_LAST_KEYWORD, null, null);
                        Toast.makeText(getActivity(), getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                        // TODO: 通信エラーの時はどうする？
                    }
                });

        VolleyApiUtils.send(getActivity().getApplicationContext(), request);
    }
}
