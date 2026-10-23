package com.example.physics

import com.example.model.Planet
import com.example.model.ShipFlightTelemetry
import com.example.model.StarSystem
import com.example.model.Vector2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class OrbitalPhysicsEngine {

    val G: Float = 0.85f // Tuned gravitational constant for game scale
    val SOFTENING: Float = 22f // Prevents singularity asymptote
    val MAX_THRUST: Float = 4.8f // Max engine acceleration
    val FUEL_BURN_RATE: Float = 3.2f // Units per second at 100% throttle

    fun updatePlanets(planets: List<Planet>, dt: Float) {
        for (planet in planets) {
            planet.currentAngle += planet.orbitSpeed * dt
            if (planet.currentAngle > 2 * PI) {
                planet.currentAngle -= (2 * PI).toFloat()
            } else if (planet.currentAngle < 0f) {
                planet.currentAngle += (2 * PI).toFloat()
            }
        }
    }

    fun stepPhysics(
        telemetry: ShipFlightTelemetry,
        starSystem: StarSystem,
        dt: Float,
        autoPrograde: Boolean = false,
        autoRetrograde: Boolean = false,
        killRotation: Boolean = false
    ) {
        // Star is at origin (0, 0)
        val starPos = Vector2D(0f, 0f)

        // Handle autopilot heading adjustments
        if (autoPrograde) {
            val targetAngle = telemetry.velocity.angle()
            telemetry.headingAngle = smoothRotate(telemetry.headingAngle, targetAngle, 3.5f * dt)
        } else if (autoRetrograde) {
            val targetAngle = telemetry.velocity.angle() + PI.toFloat()
            telemetry.headingAngle = smoothRotate(telemetry.headingAngle, targetAngle, 3.5f * dt)
        }

        // Apply engine thrust
        var thrustAcc = Vector2D(0f, 0f)
        if (telemetry.thrustPercent > 0.01f && telemetry.fuel > 0.1f) {
            val thrustMag = MAX_THRUST * telemetry.thrustPercent
            thrustAcc = Vector2D.fromAngle(telemetry.headingAngle, thrustMag)

            // Burn fuel
            telemetry.fuel = (telemetry.fuel - FUEL_BURN_RATE * telemetry.thrustPercent * dt).coerceAtLeast(0f)
        }

        // Compute gravitational acceleration
        val gravAcc = computeGravity(telemetry.position, starSystem)

        // Net acceleration
        val totalAcc = gravAcc + thrustAcc

        // Update velocity & position (Velocity Verlet / Euler integration)
        telemetry.velocity = telemetry.velocity + (totalAcc * dt)
        telemetry.position = telemetry.position + (telemetry.velocity * dt)

        // Calculate flight telemetry
        telemetry.currentSpeed = telemetry.velocity.length()
        telemetry.altitudeFromStar = telemetry.position.distanceTo(starPos) - starSystem.radius

        // Find nearest celestial body
        var nearestName = starSystem.name
        var nearestDist = telemetry.altitudeFromStar

        for (planet in starSystem.planets) {
            val pPos = planet.getPosition(starPos)
            val d = telemetry.position.distanceTo(pPos) - planet.radius
            if (d < nearestDist) {
                nearestDist = d
                nearestName = planet.name
            }
        }
        telemetry.nearestBodyName = nearestName
        telemetry.nearestBodyDistance = nearestDist.coerceAtLeast(0f)

        // Calculate orbital parameters (Specific Orbital Energy & Eccentricity)
        val r = telemetry.position.distanceTo(starPos).coerceAtLeast(1f)
        val v = telemetry.currentSpeed
        val mu = G * starSystem.mass
        val specificEnergy = (v * v / 2f) - (mu / r)

        if (specificEnergy < 0) {
            telemetry.isOrbitStable = true
            val semiMajorAxis = -mu / (2f * specificEnergy)
            // Angular momentum h = r x v
            val h = abs(telemetry.position.x * telemetry.velocity.y - telemetry.position.y * telemetry.velocity.x)
            val eccSq = (1f + (2f * specificEnergy * h * h) / (mu * mu)).coerceAtLeast(0f)
            val eccentricity = sqrt(eccSq)
            telemetry.orbitEccentricity = eccentricity
            telemetry.orbitPeriapsis = (semiMajorAxis * (1f - eccentricity) - starSystem.radius).coerceAtLeast(0f)
            telemetry.orbitApoapsis = (semiMajorAxis * (1f + eccentricity) - starSystem.radius).coerceAtLeast(0f)
        } else {
            telemetry.isOrbitStable = false
            telemetry.orbitEccentricity = 1.2f
            telemetry.orbitPeriapsis = 0f
            telemetry.orbitApoapsis = Float.POSITIVE_INFINITY
        }

        // Calculate predictive trajectory (future 75 points)
        telemetry.trajectoryPoints = computeTrajectory(telemetry.position, telemetry.velocity, starSystem, 75, 0.45f)
    }

    private fun computeGravity(pos: Vector2D, starSystem: StarSystem): Vector2D {
        var acc = Vector2D(0f, 0f)

        // Gravity from Star
        val starPos = Vector2D(0f, 0f)
        val dStar = starPos - pos
        val distSqStar = dStar.lengthSquared() + (SOFTENING * SOFTENING)
        val distStar = sqrt(distSqStar)
        val forceMagStar = (G * starSystem.mass) / distSqStar
        acc = acc + (dStar / distStar * forceMagStar)

        // Gravity from Planets (n-body perturbation)
        for (planet in starSystem.planets) {
            val pPos = planet.getPosition(starPos)
            val dPlanet = pPos - pos
            val distSqPlanet = dPlanet.lengthSquared() + (SOFTENING * SOFTENING)
            val distPlanet = sqrt(distSqPlanet)
            val forceMagPlanet = (G * planet.mass) / distSqPlanet
            acc = acc + (dPlanet / distPlanet * forceMagPlanet)
        }

        return acc
    }

    private fun computeTrajectory(
        startPos: Vector2D,
        startVel: Vector2D,
        starSystem: StarSystem,
        steps: Int,
        dt: Float
    ): List<Vector2D> {
        val path = ArrayList<Vector2D>(steps)
        var pos = startPos
        var vel = startVel

        for (i in 0 until steps) {
            val grav = computeGravity(pos, starSystem)
            vel = vel + (grav * dt)
            pos = pos + (vel * dt)
            path.add(pos)

            // Stop trajectory if impacting star
            if (pos.distanceTo(Vector2D(0f, 0f)) < starSystem.radius * 0.8f) {
                break
            }
        }
        return path
    }

    private fun smoothRotate(current: Float, target: Float, maxDelta: Float): Float {
        var diff = (target - current) % (2 * PI.toFloat())
        if (diff < -PI) diff += 2 * PI.toFloat()
        if (diff > PI) diff -= 2 * PI.toFloat()
        return current + diff.coerceIn(-maxDelta, maxDelta)
    }
}
