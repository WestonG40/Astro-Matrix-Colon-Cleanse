package com.example.model

data class ShipComponent(
    val name: String,
    val level: Int,
    val maxLevel: Int,
    val description: String,
    val bonusStat: String,
    val upgradeCostFuel: Int,
    val upgradeCostCrystals: Int
)

data class ShipInventory(
    var plasmaFuel: Int = 120,
    var maxFuel: Int = 150,
    var quantumCrystals: Int = 45,
    var darkEnergy: Int = 15,
    var exoticAlloys: Int = 20,
    var stellarDataPoints: Int = 10
)

data class ShipFlightTelemetry(
    var position: Vector2D = Vector2D(0f, -220f),
    var velocity: Vector2D = Vector2D(2.8f, 0f),
    var headingAngle: Float = 0f, // in radians
    var thrustPercent: Float = 0f, // 0f to 1f
    var fuel: Float = 100f,
    var maxFuel: Float = 100f,
    var hull: Float = 100f,
    var shields: Float = 100f,
    var currentSpeed: Float = 0f,
    var altitudeFromStar: Float = 0f,
    var nearestBodyName: String = "Stellar Core",
    var nearestBodyDistance: Float = 0f,
    var isOrbitStable: Boolean = true,
    var orbitPeriapsis: Float = 0f,
    var orbitApoapsis: Float = 0f,
    var orbitEccentricity: Float = 0f,
    var trajectoryPoints: List<Vector2D> = emptyList()
)

data class AnomalyLog(
    val id: String,
    val title: String,
    val sectorNotation: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
