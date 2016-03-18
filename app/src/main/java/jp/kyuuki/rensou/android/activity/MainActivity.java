package jp.kyuuki.rensou.android.activity;

import jp.kyuuki.rensou.android.Config;
import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.common.Logger;
import jp.kyuuki.rensou.android.fragment.DummyFragment;
import jp.kyuuki.rensou.android.fragment.PostRensouFragment;
import jp.kyuuki.rensou.android.model.User;
import jp.kyuuki.rensou.android.net.InitialData;
import jp.kyuuki.rensou.android.net.RensouApi;
import jp.kyuuki.rensou.android.net.VolleyUtils;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
//import com.google.ads.AdRequest;
//import com.google.ads.AdView;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getName();

    // 通信
    public RequestQueue mRequestQueue;

    /*
     * 状態管理
     * 
     * - 参考: http://idios.hatenablog.com/entry/2012/07/07/235137
     */
    enum State {
        INITIAL {
            @Override
            public void start(MainActivity activity) {
                activity.getInitialData();
                transit(activity, GETTING_INITIAL_DATA);
            }
        },

        GETTING_INITIAL_DATA {
            @Override
            public void successGetInitialData(MainActivity activity, InitialData data) {
                String message = null;
                String apiBaseUrl = null;
                if (data != null) {
                    message = data.getMessege();
                    apiBaseUrl = data.getApiBaseUrl();
                }

                // 初期データに API ベース URL が入っていたら、デフォルトを上書き。
                if (apiBaseUrl != null) {
                    RensouApi.BASE_URL = apiBaseUrl;
                }

                if (message != null) {
                    // お知らせダイアログ表示
                    InitialDialogFragment dailog = InitialDialogFragment.newInstance(activity.getString(R.string.app_name), message);
                    dailog.show(activity.getSupportFragmentManager(), "dialog");
                }

                User user = User.getMyUser(activity);
                if (user == null) {
                    activity.registerUser();
                    transit(activity, REGISTORING_USER);
                } else {
                    activity.startPostRensouFragment();
                    transit(activity, READY);
                }
            }

            @Override
            public void failureGetInitialData(MainActivity activity) {
                // 初期データが取得できなくても、何もなかったかのようにふるまう
                activity.startPostRensouFragment();
                transit(activity, READY);
            }
        },

        REGISTORING_USER {
            @Override
            public void successResistorUser(MainActivity activity, User user) {
                user.saveMyUser(activity);  // 永続化

                activity.startPostRensouFragment();
                transit(activity, READY);
            }
        },

        READY;

        private static void transit(MainActivity activity, State nextState) {
            Logger.w("STATE", activity.state + " -> " + nextState);
            activity.state = nextState;
        }

        public void start(MainActivity activity) {
            Logger.e("STATE", activity.state.toString());
            throw new IllegalStateException();
        }

        public void successGetInitialData(MainActivity activity, InitialData data) {
            Logger.e("STATE", activity.state.toString());
            throw new IllegalStateException();
        }

        public void failureGetInitialData(MainActivity activity) {
            Logger.e("STATE", activity.state.toString());
            throw new IllegalStateException();
        }

        public void successResistorUser(MainActivity activity, User user) {
            Logger.e("STATE", activity.state.toString());
            throw new IllegalStateException();
        }

        public void failureResistorUser(MainActivity activity) {
            Logger.e("STATE", activity.state.toString());
            throw new IllegalStateException();
        }
    }

    private State state = State.INITIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 自動でソフトキーボードが出るのを防ぐ。
        // http://y-anz-m.blogspot.jp/2010/05/android_17.html
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 

        setContentView(R.layout.activity_main);

        // AdView をリソースとしてルックアップしてリクエストを読み込む。
        // https://developers.google.com/mobile-ads-sdk/docs/android/banner_xml?hl=ja
//        AdView adView = (AdView)this.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest();
//        //adRequest.addTestDevice("6E5BC489C3B529363F063C3C74151BC7");
//        adView.loadAd(adRequest);

        // onCreate で作っちゃっていい？null に戻っちゃうことない？
        mRequestQueue = VolleyUtils.getRequestQueue(this);

        // TODO: これが違う API に行っちゃう原因？
        // 画面回転の時に下の処理を行いたくない。onRestoreInstanceState で状態を無理やり設定もしているので注意。
        if (savedInstanceState == null) {
            // アプリ起動時のみ

            // DummyFragment をレイアウトに記述しておくと、回転時に落ちる。
            // http://y-anz-m.blogspot.jp/2012/04/android-fragment-fragmenttransaction.html
            // 完全には理解できていないが「レイアウトから生成する Fragment は FragmentTransaction」に対象にしない。ということらしい。
            Fragment newFragment = new DummyFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.mainFragment, newFragment);
            ft.commit();

            state.start(this);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();

    };

    String BUNDLE_KEY_STATE = "STATE";
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // http://qiita.com/amay077/items/097f54b7dee586fadc99
        outState.putInt(BUNDLE_KEY_STATE, state.ordinal());
    };

    // http://d.hatena.ne.jp/junji_furuya0/20111028/1319783435
    // onRestoreInstanceState()は画面の回転以外では呼ばれない。らしい。
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        int index = savedInstanceState.getInt(BUNDLE_KEY_STATE);
        this.state = State.values()[index];
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 初期データが取得できるまでは何もしない。
        if (state != State.READY) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
    
    // TODO: API 通信の抽象化
    private void getInitialData() {
        String url = Config.INITIAL_DATA_URL;

        JsonObjectRequest request = new JsonObjectRequest(Method.GET, url,

            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Logger.e("HTTP", "body is " + response.toString());

                    InitialData data = InitialData.createInitialData(response);

                    state.successGetInitialData(MainActivity.this, data);
                }
            },

            new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e("HTTP", "error " + error.getMessage());

                    state.failureGetInitialData(MainActivity.this);
                }
            });

        mRequestQueue.add(request);
    }

    private void registerUser() {
        String url = RensouApi.getPostUrlRegisterUser();
        JSONObject json = RensouApi.makeRegisterUserJson();

        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, json,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Logger.e("HTTP", "body is " + response.toString());
                    User user = RensouApi.json2User(response);
                    state.successResistorUser(MainActivity.this, user);
                }
            },

            new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                    // TODO: 通信エラーの時はどうする？
                }
            });

        mRequestQueue.add(request);
    }
    
    private void startPostRensouFragment() {
        // TODO: 初期データ取得中に回転すると、ここで ava.lang.IllegalStateException: Activity has been destroyed とか言われて落ちる。
        Fragment newFragment = new PostRensouFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFragment, newFragment);
        ft.commit();
    }
    
    /*
     * お知らせダイアログ
     */
    public static class InitialDialogFragment extends DialogFragment {

        public static InitialDialogFragment newInstance(String title, String message) {
            InitialDialogFragment fragment = new InitialDialogFragment();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("message", message);
            fragment.setArguments(args);
            return fragment;
        }  

        @Override 
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            String message = getArguments().getString("message");

            return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 何もしない。
                    }})
                .create();  
        }  
    } 
}
