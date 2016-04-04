package jp.kyuuki.rensou.android.activities;

import java.util.ArrayList;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.fragments.RensouListFragment;
import jp.kyuuki.rensou.android.models.Rensou;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

/**
 * 投稿結果画面。
 */
public class PostResultActivity extends BaseActivity {
    private static final String TAG = PostResultActivity.class.getName();

    // 引数のキー
    public static final String INTENT_EXTRA_LIST = "list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(TAG, "onCreate()");
        setContentView(R.layout.activity_post_result);

        // 引数処理
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        ArrayList<Rensou> list = (ArrayList<Rensou>) intent.getSerializableExtra(INTENT_EXTRA_LIST);

        // アクションバー
        setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Logger.v(TAG, "replace Fragment");
            /*
             * フラグメント差し替え
             */
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            RensouListFragment fragment = new RensouListFragment();

            // フラグメントに渡す値をセット
            Bundle bundle = new Bundle();
            bundle.putSerializable(RensouListFragment.BUNDLE_LIST, list);
            fragment.setArguments(bundle);

            t.replace(R.id.rensouListFragment, fragment);
            t.commit();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected String getLogTag() {
        return TAG;
    }

    @SuppressLint("NewApi")
    private void setDisplayHomeAsUpEnabled(boolean b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(b);
        }
    }
}
