package com.example.myplanegame.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.myplanegame.view.GameView;

public class AutoGameObject extends GameObject{
    //每帧移动的像素数,以向下为正
    private float speed = 2;

    public AutoGameObject(Bitmap bitmap) {
        super(bitmap);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            //在y轴方向移动speed像素
            move(0, speed * gameView.getDensity());
        }
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()) {
            //检查GameObj是否超出了Canvas的范围，如果超出，则销毁子弹
            RectF canvasRecF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            RectF robjRecF = getRectF();
            if (!RectF.intersects(canvasRecF, robjRecF)) {
                destroy();
            }
        }
    }
}

