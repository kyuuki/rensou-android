package jp.kyuuki.rensou.android.views;

import android.view.MotionEvent;

// http://d.hatena.ne.jp/miruki332/20110928/1317186720
public class TouchChanger {
    private float _horizontalSpeedRate = 1.8f;
    private float _verticalSpeedRate = 1.8f;
    private float _downX;
    private float _downY;

    public void setOnTouch(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {

            float x = (ev.getX() - _downX) * _horizontalSpeedRate + _downX;
            float y = (ev.getY() - _downY) * _verticalSpeedRate + _downY;
            ev.setLocation(x, y);
        } else if (action == MotionEvent.ACTION_DOWN) {
            _downX = ev.getX();
            _downY = ev.getY();
        }
    }

    public void setSpeed(float speed) {
        this.setSpeed(speed, speed);
    }

    public void setSpeed(float xs, float ys) {
        this._horizontalSpeedRate = xs;
        this._verticalSpeedRate = ys;
    }
}
