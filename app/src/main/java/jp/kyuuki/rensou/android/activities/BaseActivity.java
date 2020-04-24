package jp.kyuuki.rensou.android.activities;

import jp.kyuuki.rensou.android.AnalyticsApplication;
import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import jp.kyuuki.rensou.android.commons.Utils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * 全画面共通。
 * 
 * - ライフサイクルの確認用。
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getName();

    Tracker mTracker;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerV("onCreate()");

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // AppCompatActivity にしてからアイコンが出なくなった。
        // 非推奨っぽいのでアイコン表示は辞めようかな。
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        LoggerV("onStart()");
        //EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoggerV("onResume()");

        mTracker.setScreenName(getLogTag());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // TODO: AdMob が入っていない Activity を考慮
        // AdMob
        AdView mAdView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onStop() {
        super.onStop();
        LoggerV("onStop()");
        //EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LoggerV("onSaveInstanceState()");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LoggerV("onRestoreInstanceState()");
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        // ランキング画面ならメニューに 「ランキング」 を表示しない
        if (this instanceof RankingActivity) {
            menu.removeItem(R.id.action_ranking);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager manager = getSupportFragmentManager();
        DialogFragment dialog;
        
        Intent intent;
        
        switch (item.getItemId()) {
        case R.id.action_ranking:
            // ランキング画面
            //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_UI_ACTION, Analysis.GA_EA_MENU_RANKING, null, null);
            intent = new Intent(this, RankingActivity.class);
            startActivity(intent);
            return true;
        case R.id.action_about:
            //EasyTracker.getTracker().sendEvent(Analysis.GA_EC_UI_ACTION, Analysis.GA_EA_MENU_ABOUT, null, null);
            // 古いやり方らしい。
//            AlertDialog.Builder ab = new AlertDialog.Builder(this);
//            AlertDialog ad = ab.create();
//            ad.setTitle(getString(R.string.action_about));
//            ad.setMessage(getString(R.string.app_name) + " " + versionName);
//            ad.show();
            dialog = new AboutDialogFragment();
            dialog.show(manager, "dialog");
            return true;
        case R.id.action_license:
            dialog = new LicenseInfomationDialogFragment();
            dialog.show(manager, "dialog");
            return true;
        case R.id.action_request:
            intent = new Intent();
            intent.setAction(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + getString(R.string.request_email)));
            //intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"kyuuki.japan+rensou@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.request_subject));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.request_text, 
                    Utils.getVersionName(this), Build.VERSION.RELEASE, Build.MANUFACTURER + " " + Build.MODEL));
            startActivity(intent);
            return true;
        case R.id.action_debug:
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }
        
        return false;
    }

    private void LoggerV(String msg) {
        Logger.v(getLogTag(), Integer.toHexString(this.hashCode()) + ": " + msg);
    }

    // 基本的に実際の Activity で上書き
    protected String getLogTag() {
        return TAG;
    }

    // http://www.jp-z.jp/changelog/2013-05-02-1.html
    // http://www.kojion.com/blog/android/dialog_fragment/
    public static class AboutDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstatnceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.action_about));
            builder.setMessage(getMessage());
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.create();
            return dialog;
        }
        
        private String getMessage() {
            Context context = getActivity();
            StringBuffer buffer = new StringBuffer();
            buffer.append(context.getString(R.string.app_name) + " " + context.getString(R.string.version) + " " + Utils.getVersionName(context) + "\n");
            buffer.append("\n");
            buffer.append(context.getString(R.string.how_to_play));
            return buffer.toString();
        }
    }
    
    public static class LicenseInfomationDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstatnceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.action_license));
            builder.setMessage(getMessage());
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.create();
            return dialog;
        }
        
        private String getMessage() {
            Context context = getActivity();
            StringBuffer buffer = new StringBuffer();
            buffer.append(context.getString(R.string.license_infomation));
            return buffer.toString();
        }
    }
}
