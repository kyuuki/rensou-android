package jp.kyuuki.rensou.android.commons;

import jp.kyuuki.rensou.android.R;
import jp.kyuuki.rensou.android.models.Rensou;
import android.content.Context;

import java.util.Date;
import java.util.TimeZone;

public final class RensouUtils {

    private RensouUtils() {}
    
    // 連想結果を View 表示用の HTML に変換する
    public static String rensouToHtml(Rensou rensou, Context context) {
        StringBuffer buf = new StringBuffer();
        buf.append("<font color='#ff0000'>");
        buf.append(rensou.getOldKeyword());
        buf.append("</font>");
        buf.append(" ");
        buf.append("<small>");
        buf.append(context.getString(R.string.list_rensou_related));
        buf.append("</small> ");
        buf.append(" ");
        buf.append("<font color='#ff0000'>");
        buf.append(rensou.getKeyword());
        buf.append("</font> ");
        
        return buf.toString();
    }

    public static String formatDateTime(Date d, Context context) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        dateFormat.setTimeZone(TimeZone.getDefault());
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        timeFormat.setTimeZone(TimeZone.getDefault());

        return dateFormat.format(d) + " " + timeFormat.format(d);
    }
}
