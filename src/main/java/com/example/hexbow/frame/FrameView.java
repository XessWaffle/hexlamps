package com.example.hexbow.frame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Swatch;

public class FrameView extends View {

    private static final int DISTANCE_BETWEEN_LAMPS = 0;

    private Frame frame;

    private int width, height;

    public FrameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int numLamps = frame.getLampSwatches().size();

        int maxWidth = (int) ((width - (DISTANCE_BETWEEN_LAMPS + Lamp.STROKE_WIDTH) * (numLamps + 1))/numLamps * Math.sqrt(3)/4);
        int maxHeight = (int) ((height - (DISTANCE_BETWEEN_LAMPS + Lamp.STROKE_WIDTH) * (numLamps + 1))/numLamps * Math.sqrt(3)/4);

        maxWidth = maxWidth > height * Math.sqrt(3)/4 ? (int) (height * (Math.sqrt(3))/4) : maxWidth;

        maxHeight = maxHeight > width * Math.sqrt(3)/4? (int) (width * (Math.sqrt(3))/4) : maxHeight;

        int hexWidth = Math.max(maxHeight, maxWidth);

        int offset = (int) 0;

        boolean vertical = height > width;


        for (int i = 0; i < frame.getLampSwatches().size(); i++) {
            if (vertical) {
                drawHexagon(canvas, frame.getSwatch(i + 1), width / 2, (i) * (height / numLamps + DISTANCE_BETWEEN_LAMPS ) + DISTANCE_BETWEEN_LAMPS + offset + height/(2 * numLamps), hexWidth);
            } else {
                drawHexagon(canvas, frame.getSwatch(i + 1), (i) * (width / numLamps + DISTANCE_BETWEEN_LAMPS) + DISTANCE_BETWEEN_LAMPS + offset + width/(2 * numLamps), height / 2, hexWidth);
            }
        }

        drawText(canvas);

    }

    private void drawText(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);

        int xPos = (canvas.getWidth() / 2);

        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
        Rect bounds = new Rect();
        textPaint.getTextBounds(frame.getId() + "", 0, 1, bounds);

        float textSize = 48f * Math.min(width/6, height/6) / (Math.max(bounds.width(), bounds.height()));

        textPaint.setTextSize(textSize);
        textPaint.setColor(Swatch.white().setAlpha(100).toInt());
        textPaint.setAntiAlias(true);

        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        canvas.drawText(frame.getId() + "", xPos, yPos, textPaint);

    }


    public void drawHexagon(Canvas canvas, Swatch s, int x, int y, int width) {

        Path path = new Path();
        path.moveTo((int)(x - width/2), (int)(y - width/2 * Math.sqrt(3))); // Top
        path.lineTo((int)(x - width/2 + width), (int)(y - width/2 * Math.sqrt(3))); // Bottom left
        path.lineTo((int)(x + width), (int)(y)); // Bottom right
        path.lineTo((int)(x - width/2 + width), (int)(y + width/2 * Math.sqrt(3)));
        path.lineTo((int)(x - width/2), (int)(y + width/2 * Math.sqrt(3)));// Back to Top
        path.lineTo((int)(x - width), (int)(y));
        path.close();

        Paint paint  = new Paint();
        paint.setStyle(Paint.Style.FILL);

        int [] colors = new int[] {s.toInt(), s.swatch(0.9).toInt(), s.swatch(0.8).toInt() };
        float [] positions = new float[] { 0.5f, 0.8f, 0.9f };

        RadialGradient gradient = new RadialGradient(x, y, width, colors, positions, Shader.TileMode.CLAMP);
        paint.setDither(true);
        paint.setShader(gradient);

        canvas.drawPath(path, paint);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Lamp.STROKE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(s.swatch(0.85).toInt());
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        width = parentWidth;
        height = parentHeight;

        setMeasuredDimension(width, height);
    }

    public Frame getFrame(){
        return frame;
    }

    public void setFrame(Frame frame){
        this.frame = frame;
        invalidate();
    }

    public void notifyViewUpdate() {
        invalidate();
    }
}
