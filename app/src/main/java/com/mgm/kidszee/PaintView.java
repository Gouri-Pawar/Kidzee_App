package com.mgm.kidszee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class PaintView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.RED;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean bitmapSet = false;

    public boolean drawingChanged = false, drawingChanging = false;

    private ArrayList<PathColorPair> paths = new ArrayList<>();
    private ArrayList<PathColorPair> undonePaths = new ArrayList<>();

    public MediaPlayer bg_music;

    // NEW: pending music ID (set by DrawingActivity)
    public int pendingMusic = -1;

    private class PathColorPair {
        Path path;
        int color;

        PathColorPair(Path path, int color) {
            this.path = path;
            this.color = color;
        }

        PathColorPair(PathColorPair other) {
            this.path = other.path;
            this.color = other.color;
        }
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawPath = new Path();
        drawPaint = new Paint();
        drawCanvas = new Canvas();
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        int brushSize = getResources().getInteger(R.integer.brush_size);
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setColor(paintColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setErase(boolean isErase) {
        if (isErase) {
            paintColor = Color.WHITE;
            drawPaint.setColor(Color.WHITE);
        }
    }

    public void startNew() {
        bitmapSet = false;
        paths.clear();
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void setBitMap(String fileName) {
        paths.clear();
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvasBitmap = BitmapFactory.decodeFile(fileName).copy(Bitmap.Config.ARGB_8888, true);
        bitmapSet = true;
        drawCanvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        invalidate();
    }

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
        drawingChanging = true;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        drawPath.lineTo(mX, mY);
        drawCanvas.drawPath(drawPath, drawPaint);
        paths.add(new PathColorPair(drawPath, paintColor));
        drawPath = new Path();
        drawingChanging = false;
        drawingChanged = true;
    }

    boolean canUndo() { return paths.size() > 0; }
    boolean canRedo() { return undonePaths.size() > 0; }

    public void onClickUndo() {
        if (canUndo()) {
            undonePaths.add(new PathColorPair(paths.remove(paths.size() - 1)));
            drawingChanged = true;
            invalidate();
        } else {
            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickRedo() {
        if (canRedo()) {
            paths.add(new PathColorPair(undonePaths.remove(undonePaths.size() - 1)));
            drawingChanged = true;
            invalidate();
        } else {
            Toast.makeText(getContext(), "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmapSet) canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

        for (PathColorPair pc : paths) {
            drawPaint.setColor(pc.color);
            canvas.drawPath(pc.path, drawPaint);
        }

        drawPaint.setColor(paintColor);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // NEW: start pending music only on first touch
                if (pendingMusic != -1) {
                    if (bg_music != null) bg_music.release();
                    bg_music = MediaPlayer.create(getContext(), pendingMusic);
                    if (bg_music != null) {
                        bg_music.setLooping(true);
                        bg_music.start();
                    }
                    pendingMusic = -1; // reset
                }
                touch_start(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;

            case MotionEvent.ACTION_UP:
                touch_up();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }
}