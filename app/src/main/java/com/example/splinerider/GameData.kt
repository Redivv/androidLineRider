package com.example.splinerider

import android.graphics.RectF
import android.provider.BaseColumns

object GameData {
    const val playerCenterOffset = 30f
    val goalRectangle = RectF(800f, 1300f, 900f, 1400f)
    val gameObstacles = arrayOf(
        RectF(0f, 0f, 0f, 0f),
        RectF(255f, 400f, 400f, 200f),
        RectF(200f, 1500f, 655f, 822f),
        RectF(750f, 600f, 950f, 200f),
        RectF(730f, 1480f, 980f, 1430f),
        RectF(255f, 700f, 600f, 650f),
        RectF(830f, 1200f, 880f, 800f),
        RectF(355f, 600f, 700f, 550f),
        RectF(730f, 1280f, 980f, 1230f),
    )
}