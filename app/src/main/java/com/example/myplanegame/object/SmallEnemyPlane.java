package com.example.myplanegame.object;

import android.graphics.Bitmap;

/**
 * 小敌机类，体积小，抗打击能力弱
 */
public class SmallEnemyPlane extends EnemyPlane {
    public SmallEnemyPlane(Bitmap bitmap){
        super(bitmap);
        setPower(1);//大敌机抗抵抗能力为10，即需要10颗子弹才能销毁大敌机
        setValue(1000);//销毁一个大敌机可以得30000分
    }
}
