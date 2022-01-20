package com.example.splinerider

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.os.VibrationEffect

import android.os.Build

import android.os.Vibrator
import androidx.appcompat.app.AlertDialog


class GameView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isMultiTouch: Boolean = false
    private var isGameRestarted: Boolean = false
    private var gameLevel = 0
    private var lineEndX: Float = 100f
    private var lineEndY: Float = 100f
    private lateinit var obstaclesArray: Array<RectF>
    private lateinit var sharedPref: SharedPreferences
    private val playerOval = RectF(lineEndX, lineEndY, 40f, 40f)
    private val goalRectangle = RectF(800f, 1300f, 900f, 1400f)
    private val playerCenterOffset = 30

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gameLevel = sharedPref.getInt("savedLevel", 0)
        obstaclesArray = GameData.gameLevels[gameLevel]
        drawPlayer(canvas)
        drawObstacles(canvas)
        drawGoal(canvas)
    }

    fun setSharedPreferences(mainSharedPref: SharedPreferences) {
        sharedPref = mainSharedPref
    }

    private fun drawGoal(canvas: Canvas) {
        paint.color = Color.YELLOW
        canvas.drawRect(goalRectangle, paint)
    }

    private fun drawObstacles(canvas: Canvas) {
        paint.color = Color.RED
        for (i in obstaclesArray.indices) {
            canvas.drawRect(obstaclesArray[i], paint)
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        playerOval.left = lineEndX - playerCenterOffset
        playerOval.top = lineEndY - playerCenterOffset
        playerOval.right = lineEndX + playerCenterOffset
        playerOval.bottom = lineEndY + playerCenterOffset

        paint.color = Color.BLACK
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE
        canvas.drawOval(playerOval, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.GREEN
        canvas.drawOval(playerOval, paint)
    }

    override fun onTouchEvent(event: MotionEvent):
            Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            val newLineEndX = event.getX(event.getPointerId(event.pointerCount - 1))
            val newLineEndY = event.getY(event.getPointerId(event.pointerCount - 1))
            isMultiTouch = event.pointerCount > 1

            var newLineStartingX = 0.0f
            var newLineStartingY = 0.0f
            if (isMultiTouch) {
                newLineStartingX = event.getX(event.getPointerId(event.pointerCount - 2))
                newLineStartingY = event.getY(event.getPointerId(event.pointerCount - 2))
            }
            var isColliding = false
            for (i in obstaclesArray.indices) {
                if (isLineCollidingWithRectangle(
                        newLineStartingX - playerCenterOffset,
                        newLineStartingY - playerCenterOffset,
                        newLineEndX,
                        newLineEndY,
                        obstaclesArray[i].left,
                        obstaclesArray[i].top,
                        obstaclesArray[i].width(),
                        obstaclesArray[i].height()
                    )
                ) {
                    isColliding = true
                    break
                }
            }
            if (!isColliding) {
                if (isLineCollidingWithRectangle(
                        newLineStartingX - playerCenterOffset,
                        newLineStartingY - playerCenterOffset,
                        newLineEndX,
                        newLineEndY,
                        goalRectangle.left,
                        goalRectangle.top,
                        goalRectangle.width(),
                        goalRectangle.height()
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
        } else if (action == MotionEvent.ACTION_MOVE) {
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
        showInfoDialog("Skucha", "Jeszcze raz")
    }

    private fun winGame() {
        vibrateDevice(800)
        lineEndX = 100f
        lineEndY = 100f
        isGameRestarted = true
        if (gameLevel == GameData.gameLevels.size - 1) {
            sharedPref.edit().putInt("savedLevel", 0).apply()
            showInfoDialog("Koniec gry", "Spróbuj jeszcze raz")
        } else {
            gameLevel++
            sharedPref.edit().putInt("savedLevel", gameLevel).apply()
            showInfoDialog("Wygrałeś", "Brawo")
        }
    }

    private fun vibrateDevice(milliseconds: Long = 200) {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(milliseconds)
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

    private fun showInfoDialog(alertTitle: String, alertMessage: String) {
        var builder = AlertDialog.Builder(context)
        builder.setTitle(alertTitle)
        builder.setMessage(alertMessage)
        builder.setCancelable(true)
        builder.setNegativeButton("OK") { dialog, id ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }
}