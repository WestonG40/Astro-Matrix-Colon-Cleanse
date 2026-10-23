package com.example.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun length(): Float = sqrt(x * x + y * y)

    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.00001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D(0f, 0f)

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    fun distanceTo(other: Vector2D): Float = (this - other).length()

    fun angle(): Float = atan2(y, x)

    fun rotate(radians: Float): Vector2D {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector2D(
            x * cosA - y * sinA,
            x * sinA + y * cosA
        )
    }

    companion object {
        fun fromAngle(angleRad: Float, magnitude: Float = 1f): Vector2D {
            return Vector2D(cos(angleRad) * magnitude, sin(angleRad) * magnitude)
        }
    }
}
