package com.shamanshs.tatar_checkers

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.shamanshs.tatar_checkers.LifeOfGameFigure.tartel
import java.io.File
import kotlin.math.abs

class GameOfLifeView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var cellSide = 0f
    var size = 16
    var mapABoard: Array<Array<Int>> = Array(size) { Array(size) { 0 } }
    init{
        fileRead()
    }
    private val whitePaint = Paint().apply {
        color = Color.parseColor("#A58E8E")
    }
    private val blackPaint = Paint().apply {
        color = Color.parseColor("#453737")
    }


    private val whitechecker = BitmapFactory.decodeResource(resources, R.drawable.white_checker)
    private val blackchecker = BitmapFactory.decodeResource(resources, R.drawable.black_checker)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawPiece(canvas)
        mapABoard = nextDay()
    }

    private fun fileRead(){
        for(i in 0..<size) {
            for (j in 0..<size){
                mapABoard[i][j] = if (tartel[(size * j) + i] == '0') 0 else 1
            }
        }
    }

    private fun nextDay(): Array<Array<Int>>{
        val nextArray = Array(size) { Array(size) { 0 } }
        for(i in 0..<size) {
            for (j in 0..<size) {
                val life = isLife(i, j)
                if (life == 3 && mapABoard[i][j] == 0)
                    nextArray[i][j] = 1
                else if ((life > 3 || life < 2) && mapABoard[i][j] == 1)
                    nextArray[i][j] = 0
                else
                    nextArray[i][j] = mapABoard[i][j]
            }
        }
        return nextArray
    }

    private fun isLife(i:Int, j:Int):Int {
        var life = 0
        for( x in -1..1){
            for( y in -1..1){
                if (!(x == 0 && y == 0)){
                    val newx = i + x
                    val newy = j + y
                    if (mapABoard[if (newx == 16) 0 else if(newx == -1) 15 else newx][if (newy == 16) 0 else if(newy == -1) 15 else newy] == 1)
                        life++
                }
            }
        }
        return  life
    }

    private fun drawPiece(canvas: Canvas) {
        for (col in 0..<size) {
            for (row in 0..<size)
                if (mapABoard[col][row] == 1)
                    canvas.drawBitmap( whitechecker, null, Rect(
                            (col * cellSide).toInt(),
                            (row * cellSide).toInt(), ((col + 1) * cellSide).toInt(),
                            ((row + 1) * cellSide).toInt()
                        ), null
            )
        }
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