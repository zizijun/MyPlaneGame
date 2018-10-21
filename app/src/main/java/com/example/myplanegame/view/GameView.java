package com.example.myplanegame.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.myplanegame.R;
import com.example.myplanegame.object.AutoGameObject;
import com.example.myplanegame.object.BigEnemyPlane;
import com.example.myplanegame.object.Bullet;
import com.example.myplanegame.object.CombatAircraft;
import com.example.myplanegame.object.GameObject;
import com.example.myplanegame.object.MiddleEnemyPlane;
import com.example.myplanegame.object.SmallEnemyPlane;

import java.util.ArrayList;
import java.util.Iterator;
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

    private void restart() {
    }

    private void resume() {
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
        //我们在每一帧都检测是否满足延迟触发单击事件的条件
        if(isSingleClick()){
            onSingleClick(touchX, touchY);
        }

        super.onDraw(canvas);
        System.out.println(status+"");
        if(status == STATUS_GAME_STARTED){
            drawGameStarted(canvas);
        }else if(status == STATUS_GAME_PAUSED){
            drawGamePaused(canvas);
        }else if(status == STATUS_GAME_OVER){
            drawGameOver(canvas);
        }
    }

    private void drawGameStarted(Canvas canvas) {

        //第一次绘制时，将战斗机移到Canvas最下方，在水平方向的中心
        if(frame == 0){
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - combatAircraft.getHeight() / 2;
            combatAircraft.centerTo(centerX, centerY);
        }

        //将spritesNeedAdded添加到sprites中
        if(objsNeedAdded.size() > 0){
            gameObjects.addAll(objsNeedAdded);
            objsNeedAdded.clear();
        }

        //检查战斗机跑到子弹前面的情况
        destroyBulletsFrontOfCombatAircraft();

        //在绘制之前先移除掉已经被destroyed的Sprite
        removeDestroyedSprites();

        //每隔30帧随机添加GameObject
        if(frame % 30 == 0){
            createRandom(canvas.getWidth());
        }
        frame++;

        //遍历sprites，绘制敌机、子弹、奖励、爆炸效果
        Iterator<GameObject> iterator = gameObjects.iterator();
        while (iterator.hasNext()){
            GameObject obj = iterator.next();

            if(!obj.isDestroyed()){
                obj.draw(canvas, paint, this);
            }

            //我们此处要判断Sprite在执行了draw方法后是否被destroy掉了
            if(obj.isDestroyed()){
                //如果Sprite被销毁了，那么从Sprites中将其移除
                iterator.remove();
            }
        }

        if(combatAircraft != null){
            //最后绘制战斗机
            combatAircraft.draw(canvas, paint, this);
            if(combatAircraft.isDestroyed()){
                //如果战斗机被击中销毁了，那么游戏结束
                status = STATUS_GAME_OVER;
            }
            //通过调用postInvalidate()方法使得View持续渲染，实现动态效果
            postInvalidate();
        }
    }

    private void createRandom(int width) {
        GameObject obj = null;
        int speed = 2;
        //callTime表示createRandomSprites方法被调用的次数
        int callTime = Math.round(frame / 30);
        if((callTime + 1) % 25 == 0){ //每25帧发送道具奖品

        }
        else{
            //发送敌机
            int[] nums = {0,0,0,0,0,1,0,0,1,0,0,0,0,1,1,1,1,1,1,2};
            int index = (int)Math.floor(nums.length*Math.random());
            int type = nums[index];
            if(type == 0){
                //小敌机
                obj = new SmallEnemyPlane(bitmaps.get(4));
            }
            else if(type == 1){
                //中敌机
                obj = new MiddleEnemyPlane(bitmaps.get(5));
            }
            else if(type == 2){
                //大敌机
                obj = new BigEnemyPlane(bitmaps.get(6));
            }
            if(type != 2){
                if(Math.random() < 0.33){
                    speed = 4;
                }
            }
        }

        if(obj != null){
            float spriteWidth = obj.getWidth();
            float spriteHeight = obj.getHeight();
            float x = (float)((width - spriteWidth) * Math.random());
            float y = -spriteHeight;
            obj.setX(x);
            obj.setY(y);
            if(obj instanceof AutoGameObject){
                AutoGameObject autoSprite = (AutoGameObject)obj;
                autoSprite.setSpeed(speed);
            }
            addGameObject(obj);
        }
    }

    //检查战斗机跑到子弹前面的情况
    private void destroyBulletsFrontOfCombatAircraft() {
        if(combatAircraft != null){
            float aircraftY = combatAircraft.getY();
            List<Bullet> aliveBullets = getAliveBullets();
            for(Bullet bullet : aliveBullets){
                //如果战斗机跑到了子弹前面，那么就销毁子弹
                if(aircraftY <= bullet.getY()){
                    bullet.destroy();
                }
            }
        }
    }

    //移除掉已经destroyed的
    private void removeDestroyedSprites(){
        Iterator<GameObject> iterator = gameObjects.iterator();
        while (iterator.hasNext()){
            GameObject s = iterator.next();
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
    }

    private void drawGamePaused(Canvas canvas) {

    }

    private void drawGameOver(Canvas canvas) {

    }

    /*-------------------------------touch------------------------------------*/

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //通过调用resolveTouchType方法，得到我们想要的事件类型
        //需要注意的是resolveTouchType方法不会返回TOUCH_SINGLE_CLICK类型
        //我们会在onDraw方法每次执行的时候，都会调用isSingleClick方法检测是否触发了单击事件
        int touchType = resolveTouchType(event);
        if(status == STATUS_GAME_STARTED){
            if(touchType == TOUCH_MOVE){
                if(combatAircraft != null){
                    combatAircraft.centerTo(touchX, touchY);
                }
            }else if(touchType == TOUCH_DOUBLE_CLICK){

            }
        }else if(status == STATUS_GAME_PAUSED){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }else if(status == STATUS_GAME_OVER){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }
        return true;
    }

    private int resolveTouchType(MotionEvent event) {
        int touchType = -1;
        int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
        if (action == MotionEvent.ACTION_MOVE) {
            long deltaTime = System.currentTimeMillis() - touchDownTime;
            if (deltaTime > singleClickDurationTime){
                //触点移动
                touchType = TOUCH_MOVE;
            }
        } else if (action == MotionEvent.ACTION_DOWN){
            //触点按下
            touchDownTime = System.currentTimeMillis();
        } else if(action == MotionEvent.ACTION_UP){
            //触点弹起
            touchUpTime = System.currentTimeMillis();
            //计算触点按下到触点弹起之间的时间差
            long downUpDurationTime = touchUpTime - touchDownTime;
            //如果此次触点按下和抬起之间的时间差小于一次单击事件指定的时间差，
            //认为发生了一次单击
            if (downUpDurationTime <= singleClickDurationTime){
                //计算这次单击距离上次单击的时间差
                long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if (twoClickDurationTime <= downUpDurationTime){
                    //如果两次单击的时间差小于一次双击事件执行的时间差，
                    //认为发生了一次双击事件
                    touchType = TOUCH_DOUBLE_CLICK;

                    lastSingleClickTime = -1;
                    touchDownTime = -1;
                    touchUpTime = -1;
                } else {
                    //如果这次形成了单击事件，但是没有形成双击事件，那么我们暂不触发此次形成的单击事件
                    //我们应该在doubleClickDurationTime毫秒后看一下有没有再次形成第二个单击事件
                    //如果那时形成了第二个单击事件，那么我们就与此次的单击事件合成一次双击事件
                    //否则在doubleClickDurationTime毫秒后触发此次的单击事件
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    //在onDraw方法中调用该方法，在每一帧都检查是不是发生了单击事件
    private boolean isSingleClick(){
        boolean singleClick = false;
        //我们检查一下是不是上次的单击事件在经过了doubleClickDurationTime毫秒后满足触发单击事件的条件
        if(lastSingleClickTime > 0){
            //计算当前时刻距离上次发生单击事件的时间差
            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if(deltaTime >= doubleClickDurationTime){
                //如果时间差超过了一次双击事件所需要的时间差，
                //那么就在此刻延迟触发之前本该发生的单击事件
                singleClick = true;
                //重置变量
                lastSingleClickTime = -1;
                touchDownTime = -1;
                touchUpTime = -1;
            }
        }
        return singleClick;
    }
    private void onSingleClick(float x, float y){
        if(status == STATUS_GAME_STARTED){
            if(isClickPause(x, y)){
                //单击了暂停按钮
                pause();
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(isClickContinueButton(x, y)){
                //单击了“继续”按钮
                resume();
            }
        }else if(status == STATUS_GAME_OVER){
            if(isClickRestartButton(x, y)){
                //单击了“重新开始”按钮
                restart();
            }
        }
    }



    private boolean isClickRestartButton(float x, float y) {
        return true;
    }

    private boolean isClickContinueButton(float x, float y) {
        return true;
    }

    private boolean isClickPause(float x, float y) {
        return true;
    }

    /*-------------------------------public------------------------------------*/
    //向Sprites中添加Sprite
    public void addGameObject(GameObject gameObject){
        objsNeedAdded.add(gameObject);
    }

    //添加得分
    public void addScore(int value){
        score += value;
    }

    public int getStatus(){
        return status;
    }

    public float getDensity(){
        return density;
    }

    public Bitmap getYellowBulletBitmap(){
        return bitmaps.get(2);
    }

    public Bitmap getBlueBulletBitmap(){
        return bitmaps.get(3);
    }

    public Bitmap getExplosionBitmap(){
        return bitmaps.get(1);
    }

    //获取处于活动状态的子弹
    public List<Bullet> getAliveBullets(){
        List<Bullet> bullets = new ArrayList<Bullet>();
        for(GameObject obj : gameObjects){
            if(!obj.isDestroyed() && obj instanceof Bullet){
                Bullet bullet = (Bullet)obj;
                bullets.add(bullet);
            }
        }
        return bullets;
    }
}
