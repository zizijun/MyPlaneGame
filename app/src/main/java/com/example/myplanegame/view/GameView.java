package com.example.myplanegame.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.example.myplanegame.R;
import com.example.myplanegame.object.CombatAircraft;
import com.example.myplanegame.object.GameObject;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    private Paint paint;
    private Paint textPaint;
    private CombatAircraft combatAircraft = null;
    private List<GameObject> gameObjects = new ArrayList<GameObject>();
    private List<GameObject> objsNeedAdded = new ArrayList<GameObject>();
    //0:combatAircraft
    //1:explosion
    //2:yellowBullet
    //3:blueBullet
    //4:smallEnemyPlane
    //5:middleEnemyPlane
    //6:bigEnemyPlane
    //7:bombAward
    //8:bulletAward
    //9:pause1
    //10:pause2
    //11:bomb
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private float density = getResources().getDisplayMetrics().density;//屏幕密度
    public static final int STATUS_GAME_STARTED = 1;//游戏开始
    public static final int STATUS_GAME_PAUSED = 2;//游戏暂停
    public static final int STATUS_GAME_OVER = 3;//游戏结束
    public static final int STATUS_GAME_DESTROYED = 4;//游戏销毁
    private int status = STATUS_GAME_DESTROYED;//初始为销毁状态
    private long frame = 0;//总共绘制的帧数
    private long score = 0;//总得分
    private float fontSize = 12;//默认的字体大小，用于绘制左上角的文本
    private float fontSize2 = 20;//用于在Game Over的时候绘制Dialog中的文本
    private float borderSize = 2;//Game Over的Dialog的边框

    //触摸事件相关的变量
    private static final int TOUCH_MOVE = 1;//移动
    private static final int TOUCH_SINGLE_CLICK = 2;//单击
    private static final int TOUCH_DOUBLE_CLICK = 3;//双击
    //一次单击事件由DOWN和UP两个事件合成，假设从down到up间隔小于200毫秒，我们就认为发生了一次单击事件
    private static final int singleClickDurationTime = 200;
    //一次双击事件由两个点击事件合成，两个单击事件之间小于300毫秒，我们就认为发生了一次双击事件
    private static final int doubleClickDurationTime = 300;
    private long lastSingleClickTime = -1;//上次发生单击的时刻
    private long touchDownTime = -1;//触点按下的时刻
    private long touchUpTime = -1;//触点弹起的时刻
    private float touchX = -1;//触点的x坐标
    private float touchY = -1;//触点的y坐标
    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle){
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GameView, defStyle, 0
        );
        a.recycle();
        //初始化paint
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //设置textPaint，设置为抗锯齿，且是粗体
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xff000000);
        fontSize = textPaint.getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        textPaint.setTextSize(fontSize);
        borderSize *= density;
    }

    public void start(int[] bitmapIds) {
        destroy();
        for (int bitmapId : bitmapIds){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }
        startWhenBitmapsReady();
    }

    private void startWhenBitmapsReady(){
        combatAircraft = new CombatAircraft(bitmaps.get(0));
        //将游戏设置为开始状态
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    public void pause() {
    }

    /*-------------------------------destroy------------------------------------*/
    private void destroyNotRecyleBitmaps(){
        //将游戏设置为销毁状态
        status = STATUS_GAME_DESTROYED;

        //重置frame
        frame = 0;

        //重置得分
        score = 0;

        //销毁战斗机
        if(combatAircraft != null){
            combatAircraft.destroy();
        }
        combatAircraft = null;

        //销毁敌机、子弹、奖励、爆炸
        for(GameObject s : gameObjects){
            s.destroy();
        }
        gameObjects.clear();
    }

    public void destroy() {
        destroyNotRecyleBitmaps();
        //释放Bitmap资源
        for(Bitmap bitmap : bitmaps){
            bitmap.recycle();
        }
        bitmaps.clear();
    }
    /*-------------------------------draw------------------------------------*/

    @Override
    protected void onDraw(Canvas canvas) {
        //第一次绘制时，将战斗机移到Canvas最下方，在水平方向的中心
        if(frame == 0){
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - combatAircraft.getHeight() / 2;
            combatAircraft.centerTo(centerX, centerY);
        }
        frame++;
        if(combatAircraft != null){
            //绘制战斗机
            combatAircraft.draw(canvas, paint, this);
            if(combatAircraft.isDestroyed()){
                //如果战斗机被击中销毁了，那么游戏结束
                status = STATUS_GAME_OVER;
            }
            //通过调用postInvalidate()方法使得View持续渲染，实现动态效果
            postInvalidate();
        }
    }

}
