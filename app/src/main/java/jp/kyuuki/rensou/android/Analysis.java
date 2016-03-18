package jp.kyuuki.rensou.android;

public final class Analysis {
    
    private Analysis() {}
    
    /*
     * Google Analytics
     */
    /* Event Category */
    public static final String GA_EC_UI_ACTION = "ui_action";
    public static final String GA_EC_ERROR = "error";
    
    /* Event Action */
    // ui_action
    public static final String GA_EA_BUTTON_POST  = "button_post";  // 投稿ボタン押下
    public static final String GA_EA_MENU_ABOUT = "menu_about";  // About メニュー選択
    public static final String GA_EA_MENU_RANKING = "menu_ranking";  // ランキングメニュー選択
    
    // error
    public static final String GA_EA_POST_CONFLICT = "post_conflict";  // 投稿がかぶった
    public static final String GA_EA_POST_ERROR = "post_error";  // 投稿時にかぶった以外のエラー
    public static final String GA_EA_POST_VALIDATE = "post_validate";  // 投稿前の入力チェックでエラー
    public static final String GA_EA_GET_LAST_KEYWORD = "get_last_keyword";  // 最新の連想取得でエラー
    
    /* Event Label */
    // 使わない
    
    /*
     * Flurry
     */
}
