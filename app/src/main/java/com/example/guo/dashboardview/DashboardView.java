package com.example.guo.dashboardview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import static com.example.guo.dashboardview.Utils.dp2px;
import static com.example.guo.dashboardview.Utils.sp2px;

/**
 * Created by guo on 2018/1/15.
 * <p>
 * 自定义仪表盘，暂时当做温度计
 */

public class DashboardView extends View {

    private int mStartAngle = 180; // 起始角度
    private int mSweepAngle = 180; // 绘制角度

    private Paint mPaint;
    private RectF mRectFArc;
    private Path mPath;
    private RectF mRectFAssistantArc;//辅助扇形，用来画刻度的
    private Rect mRectText;
    private String[] mTexts;
    private int mStrokeWidth;

    private int mMin = 0; // 最小值
    private int mMax = 100; // 最大值

    private String mNameText = "℃"; // 温度
    private int mCurrentTmp = mMin; // 当前温度

    private int mRadius;//仪表盘的半径

    private float mCenterX;
    private float mCenterY;

    private int mItem = 10; // 平分几份
    private int mPortion = 5; // 一个mSection等分份数
    private int mLength;

    private int mLengthText;
    private int mPSRadius;
    private int mPadding;
    private int mPLRadius;

    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStrokeWidth = dp2px(1);
        mLength = dp2px(8) + mStrokeWidth;
        mLengthText = mLength + dp2px(2);
        mPSRadius = dp2px(10);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFArc = new RectF();
        mPath = new Path();
        mRectFAssistantArc = new RectF();
        mRectText = new Rect();

        // 需要显示mSection + 1个刻度读数
        mTexts = new String[mItem + 1];
        for (int i = 0; i < mTexts.length; i++) {
            int n = (mMax - mMin) / mItem;
            mTexts[i] = String.valueOf(mMin + i * n);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //padding
        mPadding = Math.max(Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom()));
        setPadding(mPadding, mPadding, mPadding, mPadding);

        //计算width，默认宽为300dp
        int width = resolveSize(dp2px(300), widthMeasureSpec);

        mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2;

        mPaint.setTextSize(sp2px(16));

        //指针指向的温度值
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);

        // 由半径+指针短半径+实时读数文字高度确定的高度
        int height1 = mRadius + mStrokeWidth * 2 + mPSRadius + mRectText.height() * 3;
        // 由起始角度确定的高度
        float[] pointStart = getRightPoint(mRadius, mStartAngle);
        // 由结束角度确定的高度
        float[] pointEnd = getRightPoint(mRadius, mStartAngle + mSweepAngle);


        // 取最大值
        int max = (int) Math.max(height1,
                Math.max(pointStart[1] + mRadius + mStrokeWidth * 2, pointEnd[1] + mRadius + mStrokeWidth * 2));

        setMeasuredDimension(width, max + getPaddingTop() + getPaddingBottom());

        mCenterX = mCenterY = getMeasuredWidth() / 2f;

        //扇形的rect
        mRectFArc.set(getPaddingLeft() + mStrokeWidth,
                getPaddingTop() + mStrokeWidth,
                getMeasuredWidth() - getPaddingRight() - mStrokeWidth,
                getMeasuredWidth() - getPaddingBottom() - mStrokeWidth);

        mPaint.setTextSize(sp2px(10));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        mRectFAssistantArc.set(
                getPaddingLeft() + mLengthText + mRectText.height(),
                getPaddingTop() + mLengthText + mRectText.height(),
                getMeasuredWidth() - getPaddingRight() - mLengthText - mRectText.height(),
                getMeasuredWidth() - getPaddingBottom() - mLengthText - mRectText.height()
        );

        mPLRadius = mRadius - (mLengthText + mRectText.height() + dp2px(5));
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //防止不调用onDraw方法。
        setWillNotDraw(false);

        //画扇形
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(Color.BLACK);

        //false,不连接圆心
        canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

        //画大刻度
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        float x0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - cos));
        float y0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - sin));
        float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength) * cos);
        float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength) * sin);

        //旋转画布，所以要保存、重置
        canvas.save();
        canvas.drawLine(x0, y0, x1, y1, mPaint);
        float angle = mSweepAngle * 1f / mItem;
        for (int i = 0; i < mItem; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            canvas.drawLine(x0, y0, x1, y1, mPaint);
        }
        canvas.restore();

        //画小刻度
        canvas.save();
        mPaint.setStrokeWidth(1);
        float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength / 2f) * cos);
        float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength / 2f) * sin);
        canvas.drawLine(x0, y0, x2, y2, mPaint);
        angle = mSweepAngle * 1f / (mItem * mPortion);
        for (int i = 1; i < mItem * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue;
            }
            canvas.drawLine(x0, y0, x2, y2, mPaint);
        }
        canvas.restore();

        //画数值
        mPaint.setTextSize(sp2px(10));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mTexts.length; i++) {
            mPaint.getTextBounds(mTexts[i], 0, mTexts[i].length(), mRectText);
            float angleO = (float) (180 * mRectText.width() / 2 /
                    (Math.PI * (mRadius - mLengthText - mRectText.height())));
            mPath.reset();
            mPath.addArc(mRectFAssistantArc,
                    mStartAngle + i * (mSweepAngle / mItem) - angleO, // 正起始角度减去θ使文字居中对准长刻度
                    mSweepAngle);
            canvas.drawTextOnPath(mTexts[i], mPath, 0, 0, mPaint);
        }

        //中间的当前温度
        if (!TextUtils.isEmpty(mNameText)) {
            String value = String.valueOf(mCurrentTmp)+" "+mNameText;
            mPaint.setTextSize(sp2px(14));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.getTextBounds(value, 0, value.length(), mRectText);
            canvas.drawText(value, mCenterX, mCenterY / 2f + mRectText.height(), mPaint);
        }

        // 指针与水平线夹角
        float angleO = mStartAngle + mSweepAngle * (mCurrentTmp - mMin) / (mMax - mMin);
        float[] currentP = getRightPoint(mPLRadius, angleO);
        mPaint.setStrokeWidth(dp2px(2));
        mPath.lineTo(currentP[0], currentP[1]);
        canvas.drawLine(currentP[0], currentP[1],mCenterX,mCenterY,mPaint);

    }



    //做动画需要当前变量的get/set方法
    public int getMCurrentTmp() {
        return mCurrentTmp;
    }
    public void setMCurrentTmp(int currentTmp) {
        if (mCurrentTmp == currentTmp || currentTmp < mMin || currentTmp > mMax) {
            return;
        }
        mCurrentTmp = currentTmp;
        postInvalidate();
    }



    /**
     * 根据角度和半径算坐标
     * @param radius
     * @param angle
     * @return
     */
    public float[] getRightPoint(int radius, float angle) {
        float[] point = new float[2];
        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

}

