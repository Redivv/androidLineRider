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

    private var isMultiTouch: Boolean = false
    private var isGameRestarted: Boolean = false

    private var gameLevel = 1

    private var lineEndX: Float = 100f
    private var lineEndY: Float = 100f
    private val playerOval = RectF(lineEndX, lineEndY, 40f, 40f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var obstaclesArray = GameData.gameObstacles
    private var playerSteps = mutableListOf(arrayOf(100f, 100f))

    private lateinit var sharedPref: SharedPreferences

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showInfoDialog(
            "Witaj w Spline Rider",
            "Ułóż z dotyków palców krzywą która poprowadzi kółko do kwadrata.\nNie odrywaj palców od ekranu (możesz je przesunąć) i uważaj na czerwone przeszkody"
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gameLevel = sharedPref.getInt("savedLevel", 0)
        drawPlayer(canvas)
        drawObstacles(canvas)
        drawGoal(canvas)
    }

    fun setSharedPreferences(mainSharedPref: SharedPreferences) {
        sharedPref = mainSharedPref
    }

    private fun drawGoal(canvas: Canvas) {
        paint.color = Color.CYAN
        canvas.drawRect(GameData.goalRectangle, paint)
    }

    private fun drawObstacles(canvas: Canvas) {
        paint.color = Color.RED
        for (i in 1 until gameLevel) {
            canvas.drawRect(obstaclesArray[i], paint)
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        playerOval.left = lineEndX - GameData.playerCenterOffset
        playerOval.top = lineEndY - GameData.playerCenterOffset
        playerOval.right = lineEndX + GameData.playerCenterOffset
        playerOval.bottom = lineEndY + GameData.playerCenterOffset

        paint.color = Color.MAGENTA
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        for (i in 1 until playerSteps.size) {
            canvas.drawLine(
                playerSteps[i-1][0],
                playerSteps[i-1][1],
                playerSteps[i][0],
                playerSteps[i][1],
                paint
            )
        }
        paint.color = Color.BLACK
        paint.strokeWidth = 20f
        canvas.drawOval(playerOval, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.MAGENTA
        canvas.drawOval(playerOval, paint)
    }

    override fun onTouchEvent(event: MotionEvent):
            Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            val newLineEndX = event.getX(event.getPointerId(event.pointerCount - 1))
            val newLineEndY = event.getY(event.getPointerId(event.pointerCount - 1))
            isMultiTouch = event.pointerCount > 1

            var newLineStartingX = playerSteps[0][0]
            var newLineStartingY = playerSteps[0][1]
            if (isMultiTouch) {
                newLineStartingX = playerSteps[event.pointerCount - 1][0]
                newLineStartingY = playerSteps[event.pointerCount - 1][1]
            }
            var isColliding = false
            for (i in 1 until gameLevel) {
                if (isLineCollidingWithRectangle(
                        newLineStartingX - GameData.playerCenterOffset,
                        newLineStartingY - GameData.playerCenterOffset,
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
                        newLineStartingX - GameData.playerCenterOffset,
                        newLineStartingY - GameData.playerCenterOffset,
                        newLineEndX,
                        newLineEndY,
                        GameData.goalRectangle.left,
                        GameData.goalRectangle.top,
                        GameData.goalRectangle.width(),
                        GameData.goalRectangle.height()
                    )
                ) {
                    winGame()
                } else {
                    lineEndX = newLineEndX
                    lineEndY = newLineEndY
                    isGameRestarted = false
                    vibrateDevice(50)
                    playerSteps.add(arrayOf(newLineEndX, newLineEndY))
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
        vibrateDevice(300)
        lineEndX = 100f
        lineEndY = 100f
        isGameRestarted = true
        playerSteps = mutableListOf(arrayOf(100f, 100f))
        showInfoDialog("Kolizja", "Spróbuj jeszcze raz")
    }

    private fun winGame() {
        vibrateDevice(1000)
        lineEndX = 100f
        lineEndY = 100f
        isGameRestarted = true
        playerSteps = mutableListOf(arrayOf(100f, 100f))
        if (gameLevel == GameData.gameObstacles.size) {
            sharedPref.edit().putInt("savedLevel", 1).apply()
            showInfoDialog("Koniec gry", "Skończyłeś wszystkie poziomy\nGratulacje!")
        } else {
            gameLevel++
            sharedPref.edit().putInt("savedLevel", gameLevel).apply()
            showInfoDialog("Poziom ukończony", "Teraz zaczynasz etap nr " + gameLevel)
        }
    }

    private fun vibrateDevice(milliseconds: Long) {
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