package com.shamanshs.tatar_checkers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.scale
import com.shamanshs.tatar_checkers.engine.Board
import com.shamanshs.tatar_checkers.engine.Board.move
import com.shamanshs.tatar_checkers.engine.Board.moveChecker
import com.shamanshs.tatar_checkers.engine.BoardLogic
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.sqrt


class FieldView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var cellSide = 0f
    private var clueCircle = 0f
    private var iCurStep = 0

    var listener: Listener? = null
    val countFrame = 30
    private val whitePaint = Paint().apply {
        color = Color.parseColor("#A58E8E")
    }
    private val blackPaint = Paint().apply {
        color = Color.parseColor("#453737")
    }
    private  val choicePaint = Paint().apply {
        color = Color.parseColor("#E1DC6B")
    }
    private  val cluePaint = Paint().apply {
        color = Color.parseColor("#7050E864")
    }
    private  val clueKillPaint = Paint().apply {
        color = Color.parseColor("#70E10505")
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }
    private  val movePaint = Paint().apply {
        color = Color.parseColor("#FF305535")
    }
    private val paint = Paint()
    private val whitechecker = BitmapFactory.decodeResource(resources, R.drawable.white_checker)
    private val blackchecker = BitmapFactory.decodeResource(resources, R.drawable.black_checker)
    private val whiteking = BitmapFactory.decodeResource(resources, R.drawable.white_king)
    private val blackking = BitmapFactory.decodeResource(resources, R.drawable.black_king)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawClue(canvas)
        for (i in 0..7){
            for(j in 0..7) {
                if (!((moveChecker[0] == i && moveChecker[1] == j) || (moveChecker[2] == i && moveChecker[3] == j)) || !move)
                    drawPiece(canvas, j, i, Board.mapABoard[i][j])
            }
        }
        if (move)
            animation(canvas, Board.mapABoard[moveChecker[0]][moveChecker[1]])
    }

    private fun animation(canvas: Canvas, color: Int) {
        val newChecker = Bitmap.createScaledBitmap(if (abs(color) == 1) { if (color == 1) whitechecker else blackchecker}
        else{ if (color == 2) whiteking else blackking}, cellSide.toInt(), cellSide.toInt(), false)

        if (moveChecker[4] == 1) {
            val ptCurve = Path()
            ptCurve.moveTo(
                (moveChecker[2] * cellSide) + cellSide / 2,
                (moveChecker[3] * cellSide) + cellSide / 2
            )
            ptCurve.lineTo(
                (moveChecker[0] * cellSide) + cellSide / 2,
                (moveChecker[1] * cellSide) + cellSide / 2
            )
            val pm = PathMeasure(ptCurve, false)

            val vec = Array(2, {0})
             vec[0] = if (moveChecker[2] < moveChecker[0]) 1 else -1
            vec[1] = if (moveChecker[3] < moveChecker[1]) 1 else -1

            val cx1 = (moveChecker[0] * cellSide) + cellSide / 2
            val cy1 = (moveChecker[1] * cellSide) + cellSide / 2
            val cx2 = (moveChecker[2] * cellSide) + cellSide / 2
            val cy2 = (moveChecker[3] * cellSide) + cellSide / 2
            val dist = sqrt(((cx1 -cx2)*(cx1 -cx2) + (cy1 - cy2)*(cy1 - cy2)).toDouble())

//            Log.d("dd", "${dist}")
            val seg = (pm.length / countFrame)
            if (iCurStep <= countFrame - 8 ) {
                val r1 = RectF((moveChecker[2] * cellSide) + iCurStep * seg * vec[0],
                (moveChecker[3] * cellSide)+ iCurStep * seg * vec[1],
                ((moveChecker[2] + 1) * cellSide)+ iCurStep * seg * vec[0],
                ((moveChecker[3] + 1) * cellSide) + iCurStep * seg * vec[1])
                canvas.drawBitmap(newChecker, null, r1, paint)
                iCurStep++
                invalidate()
            }
            else{
                iCurStep = 0
                move = false
                invalidate()
            }
        }
    }

    private fun drawPiece(canvas: Canvas, row: Int, col: Int, color: Int) {
        if (abs(color) == 1)
            canvas.drawBitmap(if (color == 1) whitechecker else blackchecker
                            , null, Rect((col * cellSide).toInt(),
                            (row * cellSide).toInt(), ((col + 1) * cellSide).toInt(),
                            ((row + 1) * cellSide).toInt() ), paint)
        if (abs(color) == 2)
            canvas.drawBitmap(if (color == 2) whiteking else blackking
                , null, Rect((col * cellSide).toInt(),
                    (row * cellSide).toInt(), ((col + 1) * cellSide).toInt(),
                    ((row + 1) * cellSide).toInt() ), paint)
    }

    private  fun drawClue(canvas: Canvas) {
        if (moveChecker[4] == 1) {
            canvas.drawRect(
                moveChecker[0] * cellSide, moveChecker[1] * cellSide,
                (moveChecker[0] + 1) * cellSide, (moveChecker[1] + 1) * cellSide,
                movePaint
            )
            canvas.drawRect(
                moveChecker[2] * cellSide, moveChecker[3] * cellSide,
                (moveChecker[2] + 1) * cellSide, (moveChecker[3] + 1) * cellSide,
                movePaint
            )
        }
        for(i in 0.. 7) {
            for (j in 0..7){
                val cx = (i * cellSide) + cellSide / 2
                val cy = (j * cellSide) + cellSide / 2
                if (Board.mapMove[i][j] == 1) {
                    canvas.drawRect(i * cellSide, j * cellSide,
                        (i + 1) * cellSide, (j + 1) * cellSide,
                        choicePaint)
                }
                else if (Board.mapMove[i][j] == 3) {
                    canvas.drawCircle(cx, cy, clueCircle, clueKillPaint)
                }
                else if (Board.mapMove[i][j] == 2){
                    canvas.drawCircle(cx, cy, clueCircle, cluePaint)
                }
            }
        }
    }


    private fun drawBoard(canvas: Canvas) {
        for (i in 0..7){
            for (j in 0..7) {
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
            widthSize / 8f
        }
        else {
            widthSize = heightSize
            heightSize / 8f
        }
        clueCircle = cellSide / 4
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun TouchCell(x: Float, y:Float) {
        Log.d("Log", "$x  $y")
        val i: Int = floor((x / cellSide).toString().toDouble()).toInt()
        val j: Int = floor((y / cellSide).toString().toDouble()).toInt()
        if (Board.mapMove[i][j] == 2 || Board.mapMove[i][j] == 3)
            move = true
        listener?.onCLick(i, j)
    }

    fun update(){
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                TouchCell(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // обработка перемещения пальца по экрану
                return true
            }
            MotionEvent.ACTION_UP -> {
                // обработка отпускания пальца от экрана
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    interface Listener{
        fun onCLick(i: Int, j: Int)
    }
}