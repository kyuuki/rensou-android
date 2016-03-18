package jp.kyuuki.rensou.android;

/**
 * 実行環境。
 * 
 * - 変更の可能性のある設定値は全て Config に記述するが、環境に依存してセットで設定しなければならない値は、こちらでグループ化してから Config で設定する。
 */
public enum Environment {

    /**
     * 本番環境
     */
    PRODUCTION(
        "http://api.u1fukui.com/rensou",
        "https://s3-ap-northeast-1.amazonaws.com/kyuuki.jp/rensou.json"
    ),

    /**
     * 開発環境
     */
    DEVELOPMENT(
        "http://example.com/rensou",
        "http://example.com/rensou.json"
    );

    // Getter 作るのめんどいので public。
    public final String apiBaseUrl;
    public final String initialDataUrl;

    Environment(String apiBaseUrl, String initialDataUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.initialDataUrl = initialDataUrl;
    }
}
