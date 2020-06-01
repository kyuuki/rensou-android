package jp.kyuuki.rensou.android.activities;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.fragments.RankingListFragment;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.MenuItem;

/**
 * ランキング画面。
 */
public class RankingActivity extends BaseActivity {
    private static final String TAG = RankingActivity.class.getName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // アクションバー
        setDisplayHomeAsUpEnabled(true);

        /*
         * フラグメント差し替え
         */
        // http://www.garunimo.com/program/p17.xhtml
        if (savedInstanceState == null) {
            Fragment fragment = new RankingListFragment();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(b);
        }
    }
}
