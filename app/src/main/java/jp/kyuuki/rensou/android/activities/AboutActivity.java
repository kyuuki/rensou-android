package jp.kyuuki.rensou.android.activities;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.commons.Logger;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AboutActivity extends FragmentActivity {

    private EditText mLogEdit;
    private Button mSendButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI 初期化
        setContentView(R.layout.activity_about);
        mLogEdit = (EditText) findViewById(R.id.log_edit);
        mSendButton = (Button) findViewById(R.id.send_button);
        
        mLogEdit.setText(Logger.getMessages());
        mLogEdit.setKeyListener(null);
        
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                Uri uri = Uri.parse("mailto:");
                intent.setData(uri);
                intent.putExtra(Intent.EXTRA_TEXT, mLogEdit.getText().toString());
                startActivity(intent);
            }
        });
    }
}
