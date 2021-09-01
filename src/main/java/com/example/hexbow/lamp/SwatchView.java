package com.example.hexbow.lamp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SwatchView extends View {

    private Swatch swatch;

    private int width, height;

    public SwatchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        swatch = new Swatch((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255), 255);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int radius = (int)(Math.min(width/2, height/2) * 0.75);

        Paint paint  = new Paint();
        paint.setStyle(Paint.Style.FILL);

        int [] colors = new int[] {swatch.toInt(), swatch.swatch(0.9).toInt(), swatch.swatch(0.8).toInt() };
        float [] positions = new float[] { 0.6f, 0.7f, 0.9f };

        RadialGradient gradient = new RadialGradient(width/2, height/2, radius, colors, positions, Shader.TileMode.CLAMP);
        paint.setDither(true);
        paint.setShader(gradient);
        paint.setAntiAlias(true);

        canvas.drawCircle(width/2, height/2, radius, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        width = parentWidth;
        height = parentHeight;

        setMeasuredDimension(width, height);
    }

    public Swatch getSwatch() {
        return swatch;
    }

    public void setSwatch(Swatch swatch) {
        this.swatch = swatch;
    }
}
