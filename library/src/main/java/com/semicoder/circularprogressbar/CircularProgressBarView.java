package com.semicoder.circularprogressbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CircularProgressBarView extends View {
    private static final String TAG = CircularProgressBarView.class.getSimpleName();

    private static final int ANGLE_FULL = 360;
    private static final int DURATION = 2000;
    private static final int DURATION_CHARGING = 4500;
    private static final float FLOAT_SMALL = 0.001F;
    private static final int GAP_LENGTH = 10;
    private static final int HUNDRED = 100;
    private static final int PAINT_TEXT_SIZE = 12;
    private static final double RATE_CIRCLE = 0.6D;
    private static final double RATE_RADIUS_ENDPOINT = 0.17D;
    private static final double RATE_RADIUS_PAINT_WIDTH = 0.13D;
    private static final int ZERO_ANGLE = -90;
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mEndPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mCenterX;
    private float mCenterY;
    private float mCurrentAngle;
    private float mEndAngle;
    private int mEndPointRadius;
    private float mEndX;
    private float mEndY;
    private int mPaintWidth;
    private float mRadius;
    private float mLeft;
    private float mTop;
    private float mRight;
    private float mBottom;
    private boolean mInOpeningAnimation = false;
    private boolean mShowProcessingAnimation = false;
    private boolean mIsLowApi = false;

    private ProcessingAnimation mProcessingAnimation;
    private UpdateProgress mUpdateProgress;

    private int mBackgroundColor;
    private int mProgressColor;
    private int mEndPointColor;
    private int mTextColor;


    public CircularProgressBarView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT < 14) {
            mIsLowApi = true;
        }

        initializePaint();
    }

    public CircularProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (Build.VERSION.SDK_INT < 14) {
            mIsLowApi = true;
        }

        init(context, attrs);
    }

    public CircularProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT < 14) {
            mIsLowApi = true;
        }

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray localTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBarView, 0, 0);
        mProgressColor = localTypedArray.getColor(R.styleable.CircularProgressBarView_ProgressColor, Color.parseColor("#ff223399"));
        mBackgroundColor = localTypedArray.getColor(R.styleable.CircularProgressBarView_BackgroundColor, Color.parseColor("#55223399"));
        mEndPointColor = localTypedArray.getColor(R.styleable.CircularProgressBarView_EndPointColor, Color.parseColor("#ff223399"));
        mTextColor = localTypedArray.getColor(R.styleable.CircularProgressBarView_TextColor, Color.GREEN);
        localTypedArray.recycle();

        initializePaint();
    }

    // 计算位置
    private void computePosition() {
        int i = (int) (RATE_CIRCLE * getWidth());
        int j = (int) (RATE_CIRCLE * getHeight());

        mRadius = Math.min(i, j) / 2;
        mCenterX = (getWidth() / 2);
        mCenterY = (getHeight() / 2);
        mEndPointRadius = ((int) (RATE_RADIUS_ENDPOINT * mRadius));
        mPaintWidth = ((int) (RATE_RADIUS_PAINT_WIDTH * mRadius));
        mLeft = (mCenterX - mRadius);
        mRight = (mCenterX + mRadius);
        mTop = (mCenterY - mRadius);
        mBottom = (mCenterY + mRadius);

    }

    // 画出进度条弧线
    private void drawProgressBarArc(Canvas canvas) {
        canvas.drawArc(new RectF(mLeft, mTop, mRight, mBottom), ZERO_ANGLE, mCurrentAngle, false, mProgressPaint);
    }

    // 画出进度条上的点
    @TargetApi(11)
    private void drawProgressBarPoint(Canvas canvas) {

        float f1 = 0;

        if (mInOpeningAnimation) {
            f1 = mCurrentAngle;
        } else {
            f1 = mEndAngle;
        }

        // 计算结束点圆心
        mEndX = ((float) Math.sin(Math.toRadians(f1)) * mRadius + mCenterX);
        mEndY = ((float) Math.sin(Math.toRadians(f1) + Math.toRadians(ZERO_ANGLE)) * mRadius + mCenterY);

        if (!mIsLowApi) {
            canvas.drawCircle(mEndX, mEndY, GAP_LENGTH + mEndPointRadius, mTextPaint);
        }

        canvas.drawCircle(mEndX, mEndY, mEndPointRadius, mEndPointPaint);

        Paint.FontMetrics localFontMetrics = mTextPaint.getFontMetrics();
        float f2 = mEndX;
        float f3 = mEndY + (localFontMetrics.descent - localFontMetrics.ascent) / 2.0F - localFontMetrics.descent;
        canvas.drawText(Math.round(HUNDRED * f1 / ANGLE_FULL) + "%", f2, f3, mTextPaint);

    }

    // 初始化画笔
    @TargetApi(11)
    private void initializePaint() {

        if (!mIsLowApi) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mPaintWidth);

        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mPaintWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setAntiAlias(true);

        mEndPointPaint.setColor(mEndPointColor);
        mEndPointPaint.setStyle(Paint.Style.FILL);

        if (!mIsLowApi) {
            mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        }

        mTextPaint.setStyle(Paint.Style.FILL);
        float f = getContext().getResources().getDisplayMetrics().density;
        mTextPaint.setTextSize(0.5F + PAINT_TEXT_SIZE * f);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mTextColor);

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        computePosition();
        initializePaint();

        canvas.drawCircle(mCenterX, mCenterY, mRadius, mBackgroundPaint);
        drawProgressBarArc(canvas);
        drawProgressBarPoint(canvas);
    }

    // 设置进度条动画
    public void setProcessingAnimation(boolean isAnimation, UpdateProgress updateProgress) {

        if ((isAnimation) && (updateProgress == null)) {
            return;
        }

        mShowProcessingAnimation = isAnimation;
        mUpdateProgress = updateProgress;
    }

    // 设置进度条进度
    public void setProgress(int progress, boolean isAnimation) {
        mCurrentAngle = progress;
        mEndAngle = progress;

        if (isAnimation) {
            mInOpeningAnimation = true;
            startAnimation(new OpenAnimation(progress, DURATION));
            return;
        }

        mCurrentAngle = progress;
        invalidate();
    }

    // 设置进度条颜色
    public void setProgressColor(int color) {
        mProgressColor = color;
        mProgressPaint.setColor(mProgressColor);
    }

    // 展示进度条动画
    public void showProcessingAnimation(UpdateProgress updateProgress) {
        if (mProcessingAnimation != null) {
            return;
        }
        mProcessingAnimation = new ProcessingAnimation(DURATION_CHARGING, updateProgress);
        startAnimation(mProcessingAnimation);
    }

    // 停止进度条动画
    public void stopProcessingAnimation() {

        if (mProcessingAnimation != null) {
            clearAnimation();
            mProcessingAnimation.cancel();
            mProcessingAnimation = null;
        }

    }

    private class OpenAnimation extends Animation {
        float mmSweepAngle;

        public OpenAnimation(int sweepAngle, long duration) {
            mmSweepAngle = sweepAngle;
            setDuration(duration);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            mCurrentAngle = (interpolatedTime * mmSweepAngle);
            invalidate();

            if (1.0F - interpolatedTime < FLOAT_SMALL) {
                mInOpeningAnimation = false;
                Log.d(CircularProgressBarView.TAG, "show processing_animation is " + mShowProcessingAnimation);
            }

        }
    }

    private class ProcessingAnimation extends Animation {
        float mmSweepAngle;
        UpdateProgress mmUpdateProgress;

        private ProcessingAnimation(long duration, UpdateProgress updateProgress) {
            mmUpdateProgress = updateProgress;
            updateProgressAngle();
            setDuration(duration);
            setRepeatMode(RESTART);
            setRepeatCount(INFINITE);
        }

        private void updateProgressAngle() {
            mmSweepAngle = mmUpdateProgress.updateAngle();
            mEndAngle = mmSweepAngle;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation paramTransformation) {
            mCurrentAngle = (interpolatedTime * mmSweepAngle);
            invalidate();

            if (1.0F - interpolatedTime < FLOAT_SMALL) {
                updateProgressAngle();
            }

        }
    }

    public static abstract interface UpdateProgress {
        public abstract float updateAngle();
    }
}