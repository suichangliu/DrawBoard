package com.example.drawboard.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.drawboard.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/7/16.
 * 自定义画板控件
 */
public class DrawBoardView extends View {

    // 绘制的数据列表
    private List<List<Float>> mBoardListX;
    private List<List<Float>> mBoardListY;
    // 记录路径数组
    private Path[] mBoardPaths;
    // 多种颜色画笔数组
    private Paint[] mBoardPaints;
    // 记录当前数据绘制位置
    private int[] mPositions;
    // 调色板颜色画笔
    private Paint mColorPaint;
    // 调色板绘制位置
    private RectF mColorRectF;
    // 调色板颜色索引
    private int mColorIndex = 0;

    // 红、橙、黄、绿、青、蓝、紫
    private int[] mColorPalette = {Color.rgb(255, 0, 0), Color.rgb(255, 140, 0), Color.rgb(255, 255, 0),
            Color.rgb(0, 128, 0), Color.rgb(0, 255, 255), Color.rgb(0, 0, 255), Color.rgb(128, 0, 128)};

    // 记录按下时的X、Y坐标
    private float mDownX;
    private float mDownY;
    // 是否在画布外侧，调色板区域
    private boolean isCanvasOuter;

    public DrawBoardView(Context context) {
        super(context);
        init(context);
    }

    public DrawBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 初始化数据集合
        mBoardListX = new ArrayList<>();
        mBoardListY = new ArrayList<>();
        // 初始化绘制路径
        mBoardPaths = new Path[mColorPalette.length];
        // 初始化画笔参数
        mBoardPaints = new Paint[mColorPalette.length];
        // 初始化当前绘制位置记录
        mPositions = new int[mColorPalette.length];

        for (int i = 0; i < mColorPalette.length; i++) {
            mBoardListX.add(new ArrayList<Float>());
            mBoardListY.add(new ArrayList<Float>());
            mBoardPaths[i] = new Path();
            mBoardPaints[i] = new Paint();
            mBoardPaints[i].setFlags(Paint.ANTI_ALIAS_FLAG);
            mBoardPaints[i].setStyle(Paint.Style.STROKE);
            mBoardPaints[i].setColor(mColorPalette[i]);
            mBoardPaints[i].setStrokeWidth(DisplayUtil.dp2px(context, 3));
        }

        // 初始化调色板画笔
        mColorPaint = new Paint();
        mColorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setStyle(Paint.Style.FILL);
        mColorPaint.setTextSize(DisplayUtil.dp2px(context, 18));
        // 初始化调色板绘制位置
        mColorRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制画布底色为白色
        canvas.drawColor(Color.WHITE);

        makeColorPalette(canvas);
        makeBoardPicture(canvas);
    }

    /**
     * 绘制调色板
     */
    private void makeColorPalette(Canvas canvas) {
        mColorRectF.setEmpty();
        for (int i = 0; i < mColorPalette.length; i++) {
            mColorRectF.left = DisplayUtil.dp2px(getContext(), 20);
            mColorRectF.top = mColorRectF.bottom + DisplayUtil.dp2px(getContext(), 20);
            mColorRectF.right = DisplayUtil.dp2px(getContext(), 60);
            mColorRectF.bottom = mColorRectF.top + DisplayUtil.dp2px(getContext(), 40);
            mColorPaint.setColor(mColorPalette[i]);
            canvas.drawRect(mColorRectF, mColorPaint);
        }
        mColorPaint.setColor(Color.BLACK);
        canvas.drawText("清空", DisplayUtil.dp2px(getContext(), 23), mColorRectF.bottom + DisplayUtil.dp2px(getContext(), 60), mColorPaint);
    }

    /**
     * 绘制画布内容
     */
    private void makeBoardPicture(Canvas canvas) {
        for (int i = 0; i < mBoardListX.size(); i++) {
            for (; mPositions[i] < mBoardListX.get(i).size(); mPositions[i]++) {
                if (mBoardListX.get(i).get(mPositions[i]) < 0 && mBoardListX.get(i).size() > mPositions[i] + 1) {
                    mBoardPaths[i].moveTo(mBoardListX.get(i).get(mPositions[i] + 1), mBoardListY.get(i).get(mPositions[i] + 1));
                } else {
                    mBoardPaths[i].lineTo(mBoardListX.get(i).get(mPositions[i]), mBoardListY.get(i).get(mPositions[i]));
                }
            }
            canvas.drawPath(mBoardPaths[i], mBoardPaints[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                if (event.getX() > DisplayUtil.dp2px(getContext(), 80)) {
                    mBoardListX.get(mColorIndex).add(-1f);
                    mBoardListY.get(mColorIndex).add(-1f);
                    isCanvasOuter = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                float mUpX = event.getX();
                float mUpY = event.getY();
                int range = DisplayUtil.dp2px(getContext(), 10);
                if (mUpX < mDownX + range && mUpX > mDownX - range && mUpY < mDownY + range && mUpY > mDownY - range) {
                    colorPaletteClick(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > DisplayUtil.dp2px(getContext(), 80)) {
                    if (isCanvasOuter) {
                        isCanvasOuter = false;
                        mBoardListX.get(mColorIndex).add(-1f);
                        mBoardListY.get(mColorIndex).add(-1f);
                    }
                    mBoardListX.get(mColorIndex).add(event.getX());
                    mBoardListY.get(mColorIndex).add(event.getY());
                    invalidate();
                } else {
                    isCanvasOuter = true;
                }
                break;
        }
        return true;
    }

    /**
     * 点击调色板颜色块选择画笔颜色或清空画布
     */
    private void colorPaletteClick(MotionEvent event) {
        float downX = event.getX();
        float downY = event.getY();
        float positionX = DisplayUtil.dp2px(getContext(), 20);
        float positionY = DisplayUtil.dp2px(getContext(), 60);

        // 选取画笔颜色
        for (int i = 0; i < mColorPalette.length; i++) {
            if (downX > DisplayUtil.dp2px(getContext(), 20) && downX < DisplayUtil.dp2px(getContext(), 60)
                    && downY > positionX && downY < positionY) {
                mColorIndex = i;
                mBoardPaints[i].setColor(mColorPalette[i]);
                break;
            }
            positionX += DisplayUtil.dp2px(getContext(), 60);
            positionY += DisplayUtil.dp2px(getContext(), 60);
        }

        // 清空画布
        if (downX > DisplayUtil.dp2px(getContext(), 20) && downX < DisplayUtil.dp2px(getContext(), 60)
                && downY > DisplayUtil.dp2px(getContext(), 440) && downY < DisplayUtil.dp2px(getContext(), 500)) {
            for (int i = 0; i < mBoardListX.size(); i++) {
                mBoardListX.get(i).clear();
                mBoardListY.get(i).clear();
                mPositions[i] = 0;
                mBoardPaths[i].reset();
            }
            invalidate();
        }
    }

}