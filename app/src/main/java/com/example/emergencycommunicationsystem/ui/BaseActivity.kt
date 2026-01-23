package com.example.emergencycommunicationsystem.ui

import android.animation.ObjectAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Magnifier
import androidx.activity.ComponentActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.emergencycommunicationsystem.R
import com.example.emergencycommunicationsystem.data.UserPrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.hypot

abstract class BaseActivity : ComponentActivity() {

    private var magnifier: Magnifier? = null
    private var bubble: View? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private val longPressRunnable = Runnable {
        isLongPress = true
        bubble?.alpha = 0.1f 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val enabled = UserPrefs.isMagnifierEnabled(this@BaseActivity).first()
            if (enabled) {
                setupMagnifier()
            }
        }
    }

    protected fun setupMagnifier() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        if (bubble != null) return

        val root = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        
        val container = object : FrameLayout(this) {
            override fun performClick(): Boolean {
                return super.performClick()
            }
        }
        
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.ic_magnifier)
            setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame)
            setPadding(12, 12, 12, 12)
            alpha = 0.5f 
        }
        
        val bubbleSize = 100 
        val innerLp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(imageView, innerLp)
        
        val rootLp = FrameLayout.LayoutParams(bubbleSize, bubbleSize)
        root.addView(container, rootLp)
        bubble = container

        root.post {
            container.x = (root.width - bubbleSize).toFloat()
            container.y = (root.height / 2 - bubbleSize / 2).toFloat()
        }

        // Window size for the magnifier
        val windowSize = 600
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val magnifierBuilder = Magnifier.Builder(root)
                .setSize(windowSize, windowSize)
                .setInitialZoom(2.5f)
                .setElevation(20f)
                .setCornerRadius(windowSize / 2f) 
            
            val borderDrawable = object : Drawable() {
                private val frameColor = "#37474F".toColorInt()
                
                private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 35f 
                    color = frameColor
                }

                override fun draw(canvas: Canvas) {
                    val cx = bounds.centerX().toFloat()
                    val cy = bounds.centerY().toFloat()
                    val r = bounds.width() / 2f
                    
                    canvas.drawCircle(cx, cy, r - 17.5f, borderPaint)
                    
                    val glarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 10f
                        color = "#40FFFFFF".toColorInt()
                    }
                    val glareRect = RectF(cx - r + 60f, cy - r + 60f, cx + r - 60f, cy + r - 60f)
                    canvas.drawArc(glareRect, -110f, 50f, false, glarePaint)
                }

                override fun setAlpha(alpha: Int) {}
                override fun setColorFilter(colorFilter: ColorFilter?) {}
                @Suppress("DeprecatedCallableAddReplaceWith")
                override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
            }
            magnifierBuilder.setOverlay(borderDrawable)
            magnifier = magnifierBuilder.build()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Basic magnifier for API 28
            magnifier = Magnifier(root)
        }

        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var startX = 0f
        var startY = 0f
        var initialBubbleX = 0f
        var initialBubbleY = 0f

        container.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performClick()
                    startX = event.rawX
                    startY = event.rawY
                    initialBubbleX = v.x
                    initialBubbleY = v.y
                    isLongPress = false
                    handler.postDelayed(longPressRunnable, 300) 
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - startX
                    val deltaY = event.rawY - startY
                    
                    if (!isLongPress) {
                        v.x = initialBubbleX + deltaX
                        v.y = initialBubbleY + deltaY
                        
                        if (hypot(deltaX, deltaY) > touchSlop) {
                            handler.removeCallbacks(longPressRunnable)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            magnifier?.show(event.rawX, event.rawY)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        magnifier?.dismiss()
                    }
                    
                    v.alpha = 0.5f 
                    
                    if (!isLongPress) {
                        snapToSide(v, root.width)
                    } else {
                        snapToSide(v, root.width)
                    }
                    
                    isLongPress = false
                    true
                }
                else -> false
            }
        }
    }

    private fun snapToSide(view: View, rootWidth: Int) {
        val centerX = view.x + view.width / 2
        val targetX = if (centerX < rootWidth / 2) 0f else (rootWidth - view.width).toFloat()
        
        ObjectAnimator.ofFloat(view, "x", targetX).apply {
            duration = 300
            start()
        }
    }

    protected fun removeMagnifier() {
        bubble?.let {
            val root = window.decorView.findViewById<ViewGroup>(android.R.id.content)
            root.removeView(it)
            bubble = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.dismiss()
        }
        magnifier = null
        handler.removeCallbacks(longPressRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(longPressRunnable)
    }
}
