package com.example.myplanegame.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.myplanegame.view.GameView;

/**
 * 战斗机类，可以通过交互改变位置
 */
public class CombatAircraft extends GameObject{
    private boolean collide = false;//标识战斗机是否被击中
    private int bombAwardCount = 0;//可使用的炸弹数

    //双发子弹相关
    private boolean single = true;//标识是否发的是单一的子弹
    private int doubleTime = 0;//当前已经用双子弹绘制的次数
    private int maxDoubleTime = 140;//使用双子弹最多绘制的次数

    public CombatAircraft(Bitmap bitmap) {
        super(bitmap);
    }

    @Override
    protected void beforeDraw(Canvas canvas, Paint paint, GameView gameView) {
        if (!isDestroyed()){
            //确保战斗机完全位于Canvas范围内
            validatePosition(canvas);

            //每隔7帧发射子弹
            if(getFrame() % 7 == 0){
                fireBullet(gameView);
            }
        }
    }

    //将战机控制在画布内
    private void validatePosition(Canvas canvas) {
        if (getX() < 0){
            setX(0);
        }
        if (getY() < 0){
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if (rectF.right > canvasWidth){
            setX(canvasWidth - getWidth());
        }
        int canvasHeight = canvas.getHeight();
        if (rectF.bottom > canvasHeight) {
            setY(canvasHeight -getHeight());
        }
    }

    //发射子弹
    private void fireBullet(GameView gameView) {
        //如果战斗机被撞击了或销毁了，那么不会发射子弹
        if(collide || isDestroyed()){
            return;
        }

        float x = getX() + getWidth() / 2;
        float y = getY() - 5;
        if(single){
            //单发模式下发射单发黄色子弹
            Bitmap yellowBulletBitmap = gameView.getYellowBulletBitmap();
            Bullet yellowBullet = new Bullet(yellowBulletBitmap);
            yellowBullet.moveTo(x, y);
            gameView.addGameObject(yellowBullet);
        }
        else{
            //双发模式下发射两发蓝色子弹
            float offset = getWidth() / 4;
            float leftX = x - offset;
            float rightX = x + offset;
            Bitmap blueBulletBitmap = gameView.getBlueBulletBitmap();

            Bullet leftBlueBullet = new Bullet(blueBulletBitmap);
            leftBlueBullet.moveTo(leftX, y);
            gameView.addGameObject(leftBlueBullet);

            Bullet rightBlueBullet = new Bullet(blueBulletBitmap);
            rightBlueBullet.moveTo(rightX, y);
            gameView.addGameObject(rightBlueBullet);

            doubleTime++;
            if(doubleTime >= maxDoubleTime){
                single = true;
                doubleTime = 0;
            }
        }
    }

    public void destroy() {
    }
}
