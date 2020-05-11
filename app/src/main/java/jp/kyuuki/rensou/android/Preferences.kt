package jp.kyuuki.rensou.android

/**
 * プリファレンス関連の定数。
 */
object Preferences {
    const val NAME = "Rensou"
    const val KEY_MY_USER_ID = "MyUserId" // ユーザー ID
    const val KEY_MY_LIKES = "MyLikes" // いいね！履歴 (JSON 文字列で保持)

    // 初期データ
    const val KEY_MESSAGE = "Message"
    const val KEY_API_BASE_URL = "ApiBaseUrl"

    // GCM
    const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
    const val REGISTRATION_COMPLETE = "registrationComplete"
}