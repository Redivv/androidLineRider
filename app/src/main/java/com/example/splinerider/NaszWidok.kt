package com.example.splinerider

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.sql.Time
import kotlin.random.Random

class NaszWidok(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var kolor = Color.rgb(175, 0, 181)
    private var zielony = Color.GREEN
    private var czarny = Color.BLACK
    private var multi: Boolean = false
    private var x1: Float = 0.0f
    private var y1: Float = 0.0f
    private var r: Int = 0
    private var g: Int = 0
    private var b: Int = 0
    private lateinit var rand: Random


    fun SetRand(canvas: Canvas) {
        // x=rand.nextInt(0,canvas.width)
        //y=rand.nextInt(0,canvas.height)
        r = rand.nextInt(0, 255)
        g = rand.nextInt(0, 255)
        b = rand.nextInt(0, 255)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rand = Random(System.currentTimeMillis())

        val prost = RectF(0f, 0f, 100f, 100f)

        // for(i in 0..100) {
        SetRand(canvas)

        prost.left = x1
        prost.top = y1

        prost.right = x1 + 100
        prost.bottom = y1 + 100

        paint.color = Color.rgb(r, g, b)
        paint.style = Paint.Style.FILL

        canvas.drawRect(prost, paint)

        paint.color = czarny
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(prost, paint)

    }

    //Multitouch
    override fun onTouchEvent(event: MotionEvent):
            Boolean {
        var action = event.action and MotionEvent.ACTION_MASK
        if (action === MotionEvent.ACTION_DOWN || action === MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(
                ContentValues.TAG,
                "Touch: $action"
            )
            multi = event.pointerCount > 1
            x1 = event.getX(event.getPointerId(event.pointerCount - 1))
            y1 = event.getY(event.getPointerId(event.pointerCount - 1))
            invalidate()
            return true
        } else if (action === MotionEvent.ACTION_MOVE) {
            return true
        }
        multi = event.pointerCount > 1
        x1 = event.x
        y1 = event.y
        invalidate()
        return false
    }
}