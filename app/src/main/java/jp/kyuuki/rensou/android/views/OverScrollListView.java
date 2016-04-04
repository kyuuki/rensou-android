package jp.kyuuki.rensou.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class OverScrollListView extends ListView {

    private static int MAX_OVER_SCROLL_Y = 100;

    public OverScrollListView(Context context) {
        super(context);
    }

    public OverScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // TODO: オーバースクロールの途中で止まってしまう。
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
            int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
            scrollRangeX, scrollRangeY, maxOverScrollX, MAX_OVER_SCROLL_Y, isTouchEvent);
    }
}
