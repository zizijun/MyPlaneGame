package com.example.myplanegame.object;

import android.graphics.Bitmap;

/**
 * 子弹类，从下向上沿直线移动
 */
public class Bullet extends AutoGameObject {
    public Bullet(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10);//负数表示子弹向上飞
    }
}
