package jp.kyuuki.rensou.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONObject;

import jp.kyuuki.rensou.android.Preferences;
import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.components.InitialData;
import jp.kyuuki.rensou.android.components.VolleyApiUtils;
import jp.kyuuki.rensou.android.components.api.PostUserApi;
import jp.kyuuki.rensou.android.fragments.DummyFragment;
import jp.kyuuki.rensou.android.fragments.PostRensouFragment;
import jp.kyuuki.rensou.android.models.User;
import jp.kyuuki.rensou.android.services.RegistrationIntentService;

/**
 * 投稿画面アクティビティ。
 *
 * - 初期データ取得
 * - ユーザー登録
 */
public class MainActivity extends BaseActivity implements InitialData.Callback {

    private static final String TAG = MainActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    String BUNDLE_KEY_STATE = "STATE";  // 状態保持に使用するキー


    /*
     * ライフサイクル
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 自動でソフトキーボードが出るのを防ぐ。
        // http://y-anz-m.blogspot.jp/2010/05/android_17.html
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_main);

        // もっと、きれいにできそうな気がするけど。
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
        } else {
            // アプリ起動時以外 (回転とか)

            // onSaveInstanceState で保存した状態の復旧
            int index = savedInstanceState.getInt(BUNDLE_KEY_STATE);
            state = State.values()[index];
            Logger.d(TAG, "STATE: " + state);

            if (state != State.READY) {
                state = State.INITIAL;
                state.start(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        state.destroy(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // http://qiita.com/amay077/items/097f54b7dee586fadc99
        outState.putInt(BUNDLE_KEY_STATE, state.ordinal());
    }

    // http://d.hatena.ne.jp/junji_furuya0/20111028/1319783435
    // onRestoreInstanceState()は画面の回転以外では呼ばれない。らしい。
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // ここでやるべきじゃないと思う。onCreate に移動した。
//        int index = savedInstanceState.getInt(BUNDLE_KEY_STATE);
//        this.state = State.values()[index];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 初期データが取得できるまでは何もしない。
        if (state != State.READY) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
     * 状態管理
     * 
     * - 参考: http://idios.hatenablog.com/entry/2012/07/07/235137
     */

    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(MainActivity activity) {
                InitialData.getInitialData(activity, activity);
                transit(activity, GETTING_INITIAL_DATA);
            }
        },

        // 初期データ取得中
        GETTING_INITIAL_DATA {
            @Override
            public void successGetInitialData(MainActivity activity, InitialData data) {
                String message = null;
                if (data != null) {
                    message = data.getMessage();
                }

                if (message != null) {
                    // お知らせダイアログ表示
                    InitialDialogFragment dailog = InitialDialogFragment.newInstance(activity.getString(R.string.app_name), message);
                    dailog.show(activity.getSupportFragmentManager(), "dialog");
                }

                User user = User.getMyUser(activity);
                if (user == null) {
                    activity.registerUser();
                    transit(activity, REGISTERING_USER);
                } else {
                    activity.tryGcmRegistration();
                    activity.startPostRensouFragment();
                    transit(activity, READY);
                }
            }

            @Override
            public void failureGetInitialData(MainActivity activity) {
                // 初期データが取得できなくても、何もなかったかのようにふるまう
                activity.tryGcmRegistration();
                activity.startPostRensouFragment();
                transit(activity, READY);
            }

            @Override
            public void destroy(MainActivity activity) {
                InitialData.cancelInitialData(activity);
            }
        },

        // ユーザー登録中
        REGISTERING_USER {
            @Override
            public void successRegisterUser(MainActivity activity, User user) {
                user.saveMyUser(activity);  // 永続化

                activity.tryGcmRegistration();
                activity.startPostRensouFragment();
                transit(activity, READY);
            }

            @Override
            public void failureRegisterUser(MainActivity activity) {
                // TODO: エラー処理
                transit(activity, ERROR);
            }

            @Override
            public void destroy(MainActivity activity) {
                activity.postUserrequest.cancel();
            }
        },

        // 操作可能状態
        READY,

        // エラー状態
        ERROR;


        public void start(MainActivity activity) {
            throw new IllegalStateException();
        }

        public void successGetInitialData(MainActivity activity, InitialData data) {
            throw new IllegalStateException();
        }

        public void failureGetInitialData(MainActivity activity) {
            throw new IllegalStateException();
        }

        public void successRegisterUser(MainActivity activity, User user) {
            throw new IllegalStateException();
        }

        public void failureRegisterUser(MainActivity activity) {
            throw new IllegalStateException();
        }

        public void destroy(MainActivity activity) {
            // 通信中でなければ何もしない。
        }


        /*
         * 状態遷移 (State 内でのみ使用すること)
         */
        private static void transit(MainActivity activity, State nextState) {
            Logger.d(TAG, "STATE: " + activity.state + " -> " + nextState);
            activity.state = nextState;
        }
    }


    /**
     * ユーザー登録。
     */
    // TODO: キャンセル処理 and 他のアプリと合わせる
    JsonObjectRequest postUserrequest = null;

    private void registerUser() {
        final PostUserApi api = new PostUserApi(this);

        // Listener 使う時点で Volley 依存。
        postUserrequest = VolleyApiUtils.createJsonObjectRequest(api,

                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApiUtils.log(TAG, api, response);
                        postUserrequest = null;

                        User user = api.parseResponseBody(response);
                        state.successRegisterUser(MainActivity.this, user);
                    }
                },

                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApiUtils.log(TAG, api, error);
                        postUserrequest = null;

                        Toast.makeText(MainActivity.this, getString(R.string.error_communication), Toast.LENGTH_LONG).show();
                        // TODO: 通信エラーの時はどうする？
                        state.failureRegisterUser(MainActivity.this);
                    }
                });

        VolleyApiUtils.send(this, postUserrequest);
    }

    private void tryGcmRegistration() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Preferences.SENT_TOKEN_TO_SERVER, false) == false) {
            // ユーザー登録完了後に GCM の処理をやりっぱ。成功しても失敗しても気にしない。
            Intent intent = new Intent(this, RegistrationIntentService.class);
            this.startService(intent);
        }
    }

    private void startPostRensouFragment() {
        // TODO: 初期データ取得中に回転すると、ここで Java.lang.IllegalStateException: Activity has been destroyed とか言われて落ちる。
        // 古いアクティビティの状態遷移が動いたままになっているのが原因だと思う。
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

    /*
     * InitialData.Callback
     */

    @Override
    public void onSuccessGetInitialData(InitialData initialData) {
        state.successGetInitialData(MainActivity.this, initialData);
    }

    @Override
    public void onErrorGetInitialData() {
        state.failureGetInitialData(MainActivity.this);
    }
}
