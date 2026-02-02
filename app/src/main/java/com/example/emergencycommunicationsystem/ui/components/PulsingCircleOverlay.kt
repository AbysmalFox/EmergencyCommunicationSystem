package com.example.emergencycommunicationsystem.ui.components

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * A map overlay that renders a pulsing radar/sonar effect at a specific location.
 * Designed for high-priority alerts to indicate urgency.
 */
class PulsingCircleOverlay(
    private val center: GeoPoint,
    private val color: Int = Color.RED
) : Overlay() {

    private val paint = Paint().apply {
        this.color = color
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Animation configuration
    private val animationDuration = 3000L // 3.0 seconds per loop (much slower)
    private val maxRadiusPx = 150f // Slightly larger radius for better visibility

    override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
        if (shadow || canvas == null || mapView == null) return

        // Convert GeoPoint to screen coordinates
        val projection = mapView.projection
        val screenPoint = Point()
        projection.toPixels(center, screenPoint)

        val currentTime = System.currentTimeMillis()
        val loopTime = currentTime % animationDuration
        val progress = loopTime.toFloat() / animationDuration

        // Reset paint color to ensure it's the correct base color every frame
        // This fixes issues where alpha modifications might persist or color might be wrong
        paint.color = this.color

        // Draw multiple rings for a "sonar" effect
        // Ring 1: Main Pulse
        drawRing(canvas, screenPoint, progress)

        // Ring 2: Secondary Pulse (offset by 50% time)
        val progress2 = (loopTime + (animationDuration / 2)) % animationDuration / animationDuration.toFloat()
        drawRing(canvas, screenPoint, progress2)

        // Request redraw to animate
        mapView.postInvalidateDelayed(32) // ~30 FPS
    }

    private fun drawRing(canvas: Canvas, center: Point, progress: Float) {
        val radius = maxRadiusPx * progress
        
        // Alpha fades out as radius grows (255 -> 0)
        // Reset color first to base (implicitly alpha 255) then set alpha
        paint.color = this.color 
        val alpha = (200 * (1.0f - progress)).toInt().coerceIn(0, 255)
        paint.alpha = alpha
        
        canvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radius, paint)
    }
}
