package jp.kyuuki.rensou.android.fragment;

import jp.kyuuki.rensou.android.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ダミーフラグメント。
 * 
 * - 後で動的に差し替えるとわかっている Fragment をレイアウトファイルに記述しておくため。
 *   (Fragment クラスを指定しないとエラーになる)
 */
public class DummyFragment extends Fragment {
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dummy, container, false);
        return v;
    }
}
