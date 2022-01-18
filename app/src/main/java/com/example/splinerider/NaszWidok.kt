package com.example.splinerider

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random
import android.os.VibrationEffect

import android.os.Build

import androidx.core.content.ContextCompat.getSystemService

import android.os.Vibrator
import androidx.core.content.ContextCompat


class NaszWidok(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var kolor = Color.rgb(175, 0, 181)
    private var zielony = Color.GREEN
    private var czarny = Color.BLACK
    private var isMultiTouch: Boolean = false
    private var isGameRestarted: Boolean = false
    private var lineEndX: Float = 100f
    private var lineEndY: Float = 100f
    private var r: Int = 0
    private var g: Int = 0
    private var b: Int = 0
    private lateinit var rand: Random


    private val gracz = RectF(lineEndX, lineEndY, 40f, 40f)
    private val przeszkadzajka = RectF(255f, 400f, 400f, 200f)
    private val przeszkadzajka2 = RectF(200f, 1500f, 655f, 822f)
    private val wygrywajka = RectF(800f, 1500f, 900f, 1600f)


    fun SetRand(canvas: Canvas) {
        r = rand.nextInt(0, 255)
        g = rand.nextInt(0, 255)
        b = rand.nextInt(0, 255)
    }

    private val playerCenterOffset = 30

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rand = Random(System.currentTimeMillis())

        // for(i in 0..100) {
        SetRand(canvas)

        gracz.left = lineEndX - playerCenterOffset
        gracz.top = lineEndY - playerCenterOffset

        gracz.right = lineEndX + playerCenterOffset
        gracz.bottom = lineEndY + playerCenterOffset

        paint.color = Color.rgb(r, g, b)
        paint.style = Paint.Style.FILL
        canvas.drawOval(gracz, paint)
        canvas.drawRect(przeszkadzajka, paint)
        canvas.drawRect(przeszkadzajka2, paint)
        canvas.drawRect(wygrywajka, paint)

        paint.color = czarny
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        canvas.drawOval(gracz, paint)
    }

    override fun onTouchEvent(event: MotionEvent):
            Boolean {
        var action = event.action and MotionEvent.ACTION_MASK
        if (action === MotionEvent.ACTION_DOWN || action === MotionEvent.ACTION_POINTER_DOWN) {
            val newLineEndX = event.getX(event.getPointerId(event.pointerCount - 1))
            val newLineEndY = event.getY(event.getPointerId(event.pointerCount - 1))
            isMultiTouch = event.pointerCount > 1

            var newLineStartingX = 0.0f
            var newLineStartingY = 0.0f
            if (isMultiTouch) {
                newLineStartingX = event.getX(event.getPointerId(event.pointerCount - 2))
                newLineStartingY = event.getY(event.getPointerId(event.pointerCount - 2))
            }
            if (!isLineCollidingWithRectangle(
                    newLineStartingX - playerCenterOffset,
                    newLineStartingY - playerCenterOffset,
                    newLineEndX,
                    newLineEndY,
                    przeszkadzajka.left,
                    przeszkadzajka.top,
                    przeszkadzajka.width(),
                    przeszkadzajka.height()
                ) && !isLineCollidingWithRectangle(
                    newLineStartingX - playerCenterOffset,
                    newLineStartingY - playerCenterOffset,
                    newLineEndX,
                    newLineEndY,
                    przeszkadzajka2.left,
                    przeszkadzajka2.top,
                    przeszkadzajka2.width(),
                    przeszkadzajka2.height()
                )
            ) {
                if (isLineCollidingWithRectangle(
                        newLineStartingX - playerCenterOffset,
                        newLineStartingY - playerCenterOffset,
                        newLineEndX,
                        newLineEndY,
                        wygrywajka.left,
                        wygrywajka.top,
                        wygrywajka.width(),
                        wygrywajka.height()
                    )
                ) {
                    winGame()
                } else {
                    lineEndX = newLineEndX
                    lineEndY = newLineEndY
                    isGameRestarted = false
                }
            } else {
                restartGame()
            }
            invalidate()
            return true
        } else if (action === MotionEvent.ACTION_MOVE) {
            return true
        }
        if (!isGameRestarted) {
            restartGame()
            invalidate()
        }
        return false
    }

    private fun restartGame() {
        vibrateDevice()
        lineEndX = 100f
        lineEndY = 100f
        isGameRestarted = true
    }

    private fun winGame() {
        vibrateDevice(800)
        lineEndX = 100f
        lineEndY = 100f
        isGameRestarted = true
    }

    private fun vibrateDevice(miliseconds: Long = 200) {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    miliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(miliseconds)
        }
    }

    private fun isLineCollidingWithRectangle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        rx: Float,
        ry: Float,
        rw: Float,
        rh: Float
    ): Boolean {
        val left: Boolean = isLineCollidingWithLine(x1, y1, x2, y2, rx, ry, rx, ry + rh)
        val right: Boolean = isLineCollidingWithLine(x1, y1, x2, y2, rx + rw, ry, rx + rw, ry + rh)
        val top: Boolean = isLineCollidingWithLine(x1, y1, x2, y2, rx, ry, rx + rw, ry)
        val bottom: Boolean = isLineCollidingWithLine(x1, y1, x2, y2, rx, ry + rh, rx + rw, ry + rh)

        return left || right || top || bottom
    }

    private fun isLineCollidingWithLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float
    ): Boolean {
        val uA =
            ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        val uB =
            ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
        if (uA in 0.0..1.0 && uB >= 0 && uB <= 1) {
            return true
        }
        return false
    }
}