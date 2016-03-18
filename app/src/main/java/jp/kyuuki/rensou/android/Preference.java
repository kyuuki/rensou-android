package jp.kyuuki.rensou.android;

/**
 * プリファレンス関連の定数。
 */
public final class Preference {

    private Preference() {}
    
    public static final String NAME = "Rensou";

    public static final String KEY_MY_USER_ID = "MyUserId";  // ユーザー ID
    public static final String KEY_MY_LIKES   = "MyLikes";   // いいね！履歴 (JSON 文字列で保持)
}
