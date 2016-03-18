package jp.kyuuki.rensou.android;

public final class Config {

    private Config() {}

    public static final Environment env = Environment.PRODUCTION;

    public static final String API_BASE_URL = env.apiBaseUrl;
    public static final String INITIAL_DATA_URL = env.initialDataUrl;
}
