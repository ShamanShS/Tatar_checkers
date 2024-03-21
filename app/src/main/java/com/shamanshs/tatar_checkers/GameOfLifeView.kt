package com.shamanshs.tatar_checkers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GameOfLifeView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var cellSide = 0f
    private val whitePaint = Paint().apply {
        color = Color.parseColor("#A58E8E")
    }
    private val blackPaint = Paint().apply {
        color = Color.parseColor("#453737")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        for (i in 0..15){
            for (j in 0..15) {
                canvas.drawRect(i * cellSide, j * cellSide,
                    (i + 1) * cellSide, (j + 1) * cellSide,
                    if ((i + j) % 2 == 1) blackPaint else whitePaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        cellSide = if (widthSize <= heightSize){
            heightSize = widthSize
            widthSize / 16f
        }
        else {
            widthSize = heightSize
            heightSize / 16f
        }
        setMeasuredDimension(widthSize, heightSize)
    }
}