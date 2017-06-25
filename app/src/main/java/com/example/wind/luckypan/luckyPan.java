package com.example.wind.luckypan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * Created by wind on 17-6-24.
 */

public class luckyPan extends SurfaceView implements Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    //用于绘制的线程
    private Thread t;
    //线程的控制开关
    private boolean isRunning;
    //盘块的奖项
    private String[] mStrs = new String[]{"单反相机", "IPAD", "恭喜发财", "IPHONE", "服装一套", "恭喜发财"};
    //盘块的图片
    private int[] mImgs = new int[]{R.drawable.danfan, R.drawable.ipad, R.drawable.xiaolian,
            R.drawable.iphone, R.drawable.meizi, R.drawable.ganga};

    //与图片对应的bitmap数组
    private Bitmap[] mImgsBitmap;
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);

    //盘块的颜色
    private int[] mColors = new int[]{0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01};

    private int mItemCount = 6;
    //绘制盘块的画笔
    private Paint mArcPaint;
    //绘制文本的画笔
    private Paint mTextPaint;
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
            getResources().getDisplayMetrics());

    //整个盘块的范围
    private RectF mRange = new RectF();
    //盘块的直径
    private int mRadius;
    //转盘的中心位置
    private int mCenter;
    //padding直接以paddingLeft为准
    private int mPadding;

    //盘块滚动的速度
    private double mSpeed = 0;
    //volatile保证线程间变量可见性
    private volatile float mStartAngle = 0;
    //判断是否点击了停止按钮
    private boolean isShouldEnd;

    /*public SurfaceViewTemplate(Context context) {
        super(context);改成一个参数构造方法调用两个参数构造方法
    }*/

    public luckyPan(Context context) {
        this(context, null);
    }

    public luckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHolder = getHolder();

        mHolder.addCallback(this);

        //可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常量
        setKeepScreenOn(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());

        mPadding = getPaddingLeft();
        //直径
        mRadius = width - mPadding * 2;
        //中心点
        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //初始化绘制盘块的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        //初始化文本画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        //初始化盘块绘制的范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);

        //初始化图片
        mImgsBitmap = new Bitmap[mItemCount];
        //！！
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        isRunning = true;
        t = new Thread(this);
        t.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void run() {
        //不断进行绘制
        while (isRunning) {
            //设置50ms绘制一次盘块
            long start = System.currentTimeMillis();

            draw();

            long end = System.currentTimeMillis();

            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    //核心,draw方法,canvas的API
    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();

            if (mCanvas != null) {
                //draw something
                //绘制背景
                drawBg();

                //绘制盘块
                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);
                    //绘制盘块
                    //参数：区域，起始角度，每个角度，是否使用中间原点，画笔
                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);

                    //绘制文本
                    drawText(tmpAngle, sweepAngle, mStrs[i]);

                    //绘制Icon
                    drawIcon(tmpAngle, mImgsBitmap[i]);

                    tmpAngle += sweepAngle;
                }

                mStartAngle += mSpeed;
                //如果点击了停止按钮
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }

    //点击启动旋转~~~~~~~~~~~概率需要自己在外面套设置随机数1~1000,if 1~100,index=1....
    public void luckyStart(int index) {

        //计算每一项的角度
        float angle = 360 / mItemCount;
        //控制转盘停止的位置!!
        //计算每一项中奖范围(当前index)
        //1 -> 150~210
        //0 -> 210-270
        float from = 270 - (index + 1) * angle;
        float end = from + angle;

        //设置停下来需要旋转的距离,心机！！！
        float targetFrom = 4 * 360 + from;
        float targetEnd = 4 * 360 + end;

        /*速度计算
        v1->0,且每次-1
        (v1+0)*(v1+1)/2=targetFrom;
        v1*v1+v1-2*targetFrom=0;
        v1=(-1+Math.sqrt(1+8*targetFrom))/2
        */
        float v1= (float) ((Math.sqrt(1+8*targetFrom)-1)/2);
        float v2= (float) ((Math.sqrt(1+8*targetEnd)-1)/2);

        mSpeed=v1+Math.random()*(v2-v1);
        //mSpeed=v2;测试
//        mSpeed = 50;
        isShouldEnd = false;

    }

    public void luckyEnd() {
        isShouldEnd = true;
        //点击停止时,按角度为0计算
        mStartAngle=0;
    }

    //转盘是否在旋转
    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }


    //绘制Icon,图片中心点坐标 x=mCenter+r*cos(a), y=mCenter+r*sin(a)
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 8;

        //Math.PI/180
        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        //确定图片位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);

    }

    //绘制每个盘块的文本
    private void drawText(float tmpAngle, float sweepAngle, String string) {
        Path path = new Path();
        //弧形
        path.addArc(mRange, tmpAngle, sweepAngle);
        //参数：文字,path,水平偏移量:边上，垂直偏移量：往圆心方向,画笔
        //这边偏移量需要计算而不是写死,利用水平偏移量让文字居中
        float textWidth = mTextPaint.measureText(string);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        int vOffset = mRadius / 2 / 6;

        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    //绘制背景
    private void drawBg() {
        mCanvas.drawColor(0xffffffff);
        mCanvas.drawBitmap(mBgBitmap, null, new RectF(mPadding / 2, mPadding / 2,
                getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);
    }
}

















