package com.sunil.ffmaxsim

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        setContentView(TrainingArena(this))
    }
}

private data class Bot(var x: Float, var z: Float, var alive: Boolean = true)

class TrainingArena(context: android.content.Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bots = mutableListOf<Bot>()
    private var yaw = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var sensitivity = 65
    private var showEsp = true
    private var showRadar = true
    private var headshotMode = false
    private var hits = 0
    private var shots = 0
    private var lastTap = 0L
    private var toast = "DRAG LEFT TO MOVE  •  DRAG RIGHT TO AIM"
    private var toastUntil = System.currentTimeMillis() + 3500
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cyan = Color.rgb(54, 242, 193)
    private val red = Color.rgb(255, 87, 112)
    private val white = Color.rgb(238, 246, 255)
    private val dark = Color.rgb(8, 14, 24)
    private var targetX = 0f
    private var targetY = 0f

    init { repeat(7) { bots += Bot(Random.nextFloat() * 16f - 8f, Random.nextFloat() * 24f + 7f) } }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat(); val h = height.toFloat()
        drawBackground(c, w, h)
        drawArena(c, w, h)
        drawBots(c, w, h)
        drawCrosshair(c, w, h)
        drawHud(c, w, h)
        drawControls(c, w, h)
        if (System.currentTimeMillis() < toastUntil) {
            panel(c, w * .25f, h * .84f, w * .75f, h * .94f)
            text(c, toast, w * .5f, h * .902f, 13f, cyan, true, Paint.Align.CENTER)
        }
        postInvalidateDelayed(33)
    }

    private fun drawBackground(c: Canvas, w: Float, h: Float) {
        p.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(12, 26, 44), Color.rgb(17, 34, 43), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p); p.shader = null
        p.color = Color.rgb(27, 54, 57); c.drawRect(0f, h*.48f, w, h, p)
        p.color = Color.rgb(33, 66, 67); c.drawRect(0f, h*.48f, w, h*.51f, p)
    }

    private fun drawArena(c: Canvas, w: Float, h: Float) {
        val horizon = h*.48f
        p.color = Color.argb(95, 90, 205, 181); p.strokeWidth = 1f
        for (i in -12..12) {
            val bottomX = w/2 + i*w*.075f
            c.drawLine(w/2, horizon, bottomX, h, p)
        }
        for (j in 1..12) {
            val t = j/12f; val y = horizon + (h-horizon)*t*t
            c.drawLine(0f, y, w, y, p)
        }
        // Practice structures give the scene depth and a simple 3D perspective.
        for (i in 0..3) {
            val x = w*(.08f + i*.23f); val top = h*(.35f + (i%2)*.035f)
            p.color = Color.rgb(31, 57, 70); c.drawRoundRect(x, top, x+w*.11f, h*.56f, 7f, 7f, p)
            p.color = Color.rgb(54, 91, 98); c.drawRect(x, top, x+w*.11f, top+5f, p)
            p.color = Color.argb(100, 54, 242, 193); c.drawLine(x+10, top+18, x+w*.11f-10, top+18, p)
        }
    }

    private fun project(bot: Bot, w: Float, h: Float): Pair<Float, Float> {
        val rel = bot.x - yaw * .035f
        val depth = (bot.z + 2f).coerceAtLeast(3f)
        val scale = (h * .8f / depth).coerceIn(5f, h*.17f)
        return Pair(w/2 + rel * scale * .75f, h*.49f + scale*.08f)
    }

    private fun drawBots(c: Canvas, w: Float, h: Float) {
        bots.filter { it.alive }.forEachIndexed { idx, bot ->
            val (x, y) = project(bot, w, h); val depth = (bot.z+2f)
            val size = (h*.36f/depth).coerceIn(17f, h*.12f)
            p.color = Color.rgb(65, 85, 92); c.drawRoundRect(x-size*.22f, y-size*.9f, x+size*.22f, y, size*.12f, size*.12f, p)
            p.color = Color.rgb(111, 139, 145); c.drawCircle(x, y-size*1.04f, size*.19f, p)
            p.color = Color.argb(180, 18, 29, 39); c.drawRect(x-size*.48f, y+2, x+size*.48f, y+size*.12f, p)
            if (showEsp) {
                p.color = cyan; p.style = Paint.Style.STROKE; p.strokeWidth = 2f
                val top = y-size*1.28f; val bottom = y+size*.1f
                val bw = size*.72f
                c.drawLine(x-bw, top, x-bw*.55f, top, p); c.drawLine(x-bw, top, x-bw, top+size*.28f, p)
                c.drawLine(x+bw, top, x+bw*.55f, top, p); c.drawLine(x+bw, top, x+bw, top+size*.28f, p)
                c.drawLine(x-bw, bottom, x-bw*.55f, bottom, p); c.drawLine(x-bw, bottom, x-bw, bottom-size*.28f, p)
                c.drawLine(x+bw, bottom, x+bw*.55f, bottom, p); c.drawLine(x+bw, bottom, x+bw, bottom-size*.28f, p)
                p.style = Paint.Style.FILL
                text(c, "BOT-${idx+1}  ${bot.z.toInt()}m", x, top-9, 11f, cyan, true, Paint.Align.CENTER)
                p.color = Color.argb(160, 0, 0, 0); c.drawRect(x-25, top+3, x+25, top+7, p)
                p.color = red; c.drawRect(x-25, top+3, x+25, top+7, p)
            }
        }
    }

    private fun drawCrosshair(c: Canvas, w: Float, h: Float) {
        val x = w/2 + targetX; val y = h*.49f + targetY
        p.color = cyan; p.strokeWidth = 2f
        c.drawCircle(x, y, 15f, p); c.drawLine(x-23, y, x-7, y, p); c.drawLine(x+7, y, x+23, y, p)
        c.drawLine(x, y-23, x, y-7, p); c.drawLine(x, y+7, x, y+23, p)
        p.color = Color.WHITE; c.drawCircle(x, y, 2f, p)
    }

    private fun drawHud(c: Canvas, w: Float, h: Float) {
        panel(c, 18f, 16f, 240f, 84f)
        text(c, "FF MAX  /  TRAINING LAB", 32f, 42f, 15f, cyan, true)
        text(c, "OFFLINE SIMULATOR  •  v1.0", 32f, 65f, 10f, white)
        panel(c, w-190f, 16f, w-18f, 84f)
        text(c, "ACCURACY", w-175f, 40f, 10f, white)
        val accuracy = if (shots == 0) 0 else hits*100/shots
        text(c, "$accuracy%     $hits / $shots", w-175f, 65f, 17f, cyan, true)
        if (showRadar) drawRadar(c, w-120f, h*.30f)
        panel(c, 18f, h-94f, 245f, h-18f)
        text(c, "SENSITIVITY", 32f, h-68f, 10f, white, true)
        text(c, "$sensitivity", 32f, h-39f, 23f, cyan, true)
        text(c, "−     TAP / +", 105f, h-42f, 11f, white)
        panel(c, w-250f, h-94f, w-18f, h-18f)
        text(c, if (headshotMode) "HEADSHOT DRILL" else "AIM PRACTICE", w-232f, h-65f, 12f, cyan, true)
        text(c, "TAP TARGET  •  RESET ON HIT", w-232f, h-39f, 10f, white)
    }

    private fun drawRadar(c: Canvas, cx: Float, cy: Float) {
        p.color = Color.argb(205, 5, 14, 23); c.drawCircle(cx, cy, 72f, p)
        p.color = cyan; p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f
        c.drawCircle(cx, cy, 72f, p); c.drawCircle(cx, cy, 48f, p); c.drawCircle(cx, cy, 24f, p)
        c.drawLine(cx-72, cy, cx+72, cy, p); c.drawLine(cx, cy-72, cx, cy+72, p); p.style = Paint.Style.FILL
        bots.filter { it.alive }.forEach { b ->
            val x = (cx + b.x*7f).coerceIn(cx-63, cx+63); val y = (cy + b.z*3f-55f).coerceIn(cy-63, cy+63)
            p.color = red; c.drawCircle(x, y, 4f, p)
        }
        p.color = cyan; c.drawCircle(cx, cy, 5f, p)
        text(c, "RADAR", cx, cy+91f, 10f, cyan, true, Paint.Align.CENTER)
    }

    private fun drawControls(c: Canvas, w: Float, h: Float) {
        p.color = Color.argb(55, 54, 242, 193); p.style = Paint.Style.STROKE; p.strokeWidth = 2f
        c.drawCircle(92f, h*.68f, 48f, p); c.drawCircle(w-92f, h*.68f, 48f, p); p.style = Paint.Style.FILL
        p.color = Color.argb(130, 54, 242, 193); c.drawCircle(92f+moveX*25f, h*.68f+moveY*25f, 15f, p)
        text(c, "MOVE", 92f, h*.68f+70f, 9f, white, true, Paint.Align.CENTER)
        text(c, "AIM", w-92f, h*.68f+70f, 9f, white, true, Paint.Align.CENTER)
        // Mode toggles
        chip(c, w*.39f, 18f, if (showEsp) "ESP ON" else "ESP OFF", cyan)
        chip(c, w*.50f, 18f, if (showRadar) "RADAR ON" else "RADAR OFF", cyan)
        chip(c, w*.61f, 18f, if (headshotMode) "HEADSHOT ON" else "HEADSHOT", red)
    }

    private fun chip(c: Canvas, cx: Float, y: Float, label: String, color: Int) {
        p.color = Color.argb(185, 8, 18, 29); c.drawRoundRect(cx-49, y, cx+49, y+34, 10f, 10f, p)
        p.color = color; p.style = Paint.Style.STROKE; p.strokeWidth = 1.2f; c.drawRoundRect(cx-49, y, cx+49, y+34, 10f, 10f, p); p.style = Paint.Style.FILL
        text(c, label, cx, y+22f, 10f, color, true, Paint.Align.CENTER)
    }

    private fun panel(c: Canvas, l: Float, t: Float, r: Float, b: Float) {
        p.color = Color.argb(218, 7, 16, 27); c.drawRoundRect(l, t, r, b, 13f, 13f, p)
        p.color = Color.argb(90, 54, 242, 193); p.style = Paint.Style.STROKE; p.strokeWidth = 1f; c.drawRoundRect(l, t, r, b, 13f, 13f, p); p.style = Paint.Style.FILL
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) {
        p.color = color; p.textSize = size; p.typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL); p.textAlign = align; c.drawText(s, x, y, p)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w = width.toFloat(); val h = height.toFloat()
        when (e.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (e.x < w*.25f && e.y > h*.48f) { moveX = ((e.x-92f)/45f).coerceIn(-1f,1f); moveY = ((e.y-h*.68f)/45f).coerceIn(-1f,1f) }
                else if (e.x > w*.72f && e.y > h*.48f) { targetX = ((e.x-w*.5f)*.35f).coerceIn(-w*.18f,w*.18f); targetY = ((e.y-h*.49f)*.25f).coerceIn(-h*.12f,h*.12f); yaw += (e.x-px)*.004f }
                invalidate(); px=e.x; return true
            }
            MotionEvent.ACTION_UP -> {
                moveX=0f; moveY=0f
                if (e.y < 62f && e.x in w*.34f..w*.45f) showEsp=!showEsp
                else if (e.y < 62f && e.x in w*.45f..w*.56f) showRadar=!showRadar
                else if (e.y < 62f && e.x in w*.56f..w*.70f) headshotMode=!headshotMode
                else if (e.y > h-105f && e.x < 260f) { sensitivity = (sensitivity + if (e.x < 120f) -5 else 5).coerceIn(1,100) }
                else if (e.x > w*.55f && e.y < h*.80f) shoot(e.x, e.y)
                invalidate(); return true
            }
        }
        return true
    }
    private var px=0f
    private fun shoot(x: Float, y: Float) {
        shots++
        val w=width.toFloat(); val h=height.toFloat()
        val hit = bots.filter { it.alive }.minByOrNull { b -> val (bx,by)=project(b,w,h); hypot((bx-x).toDouble(), (by-hypot((h*.36f/(b.z+2f)).coerceIn(17f,h*.12f).toDouble(),0.0)).toDouble()).toFloat() }
        if (hit != null) {
            val (bx,by)=project(hit,w,h); val dist=hypot(bx-x, by-y)
            if (dist < h*.10f) { hit.alive=false; hits++; toast=if(headshotMode) "HEADSHOT! NICE SHOT" else "TARGET HIT!"; toastUntil=System.currentTimeMillis()+1300; if(bots.none{it.alive}) repeat(7){bots += Bot(Random.nextFloat()*16f-8f,Random.nextFloat()*24f+7f)} }
        }
    }
}
