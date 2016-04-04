package jp.kyuuki.rensou.android.models;

import jp.kyuuki.rensou.android.Preference;
import android.content.Context;
import android.content.SharedPreferences;

public class User {
    long id;
    
    public User(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static User getMyUser(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Preference.NAME, Context.MODE_PRIVATE);
        long myUserId = settings.getLong(Preference.KEY_MY_USER_ID, -1);
        
        if (myUserId < 0) {
            return null;
        } else {
            return new User(myUserId);
        }
    }
    
    public void saveMyUser(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Preference.NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(Preference.KEY_MY_USER_ID, this.getId());
        edit.commit();
    }
}
