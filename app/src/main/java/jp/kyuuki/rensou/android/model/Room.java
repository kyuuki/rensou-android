package jp.kyuuki.rensou.android.model;

import android.content.Context;
import jp.kyuuki.rensou.android.R;

public enum Room {
    STUDENT_ROOM(
        "room_name_student",
        1
    ),
    ADULT_ROOM(
        "room_name_adult",
        2
    ),
    GIRLS_ROOM(
        "room_name_girls",
        3
    ),
    OTAKU_ROOM(
        "room_name_otaku",
        4
    ),
    SECRET_ROOM(
        "room_name_secret",
        5
    );

    String nameKey;
    int apiRoomType;
    
    Room(String nameKey, int apiRoomType) {
        this.nameKey = nameKey;
        this.apiRoomType = apiRoomType;
        //context.getString(R.string.request_subject);
    }
    
    public String getRoomName(Context context) {
        return context.getString(R.string.request_subject);
    }
}
