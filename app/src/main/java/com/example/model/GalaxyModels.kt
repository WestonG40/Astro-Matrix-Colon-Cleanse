package com.example.model

enum class StarSpectralClass(
    val code: String,
    val title: String,
    val colorHex: Long,
    val baseMass: Float,
    val temperatureK: Int
) {
    BLUE_HYPERGIANT("O", "Class O Blue Hypergiant", 0xFF64B5F6, 12000f, 35000),
    WHITE_DWARF("A", "Class A White Dwarf", 0xFFE0E0E0, 7500f, 10000),
    SOL_DWARF("G", "Class G Yellow Star", 0xFFFFD54F, 9000f, 5800),
    RED_GIANT("M", "Class M Red Giant", 0xFFFF7043, 8500f, 3200),
    PULSAR("PSR", "Pulsar Neutron Star", 0xFFB388FF, 15000f, 50000),
    BLACK_HOLE("BH", "Quantum Singularity (Black Hole)", 0xFF7C4DFF, 25000f, 0)
}

enum class PlanetType(val label: String, val baseColorHex: Long) {
    TERRESTRIAL("Terrestrial Planet", 0xFF4CAF50),
    OCEAN_WORLD("Oceanic World", 0xFF0288D1),
    ICE_WORLD("Glacial Cryo-World", 0xFF80DEEA),
    GAS_GIANT("Ringed Gas Giant", 0xFFFFB74D),
    LAVA_WORLD("Volcanic Molten World", 0xFFE64A19),
    MATRIX_CORE("Crystalline Matrix World", 0xFF00E5FF),
    FORGOTTEN_RUIN("Ancient Alien Megastructure", 0xFFE040FB)
}

enum class ResourceType(val label: String, val symbol: String, val colorHex: Long) {
    PLASMA_FUEL("Plasma Fuel", "⚡", 0xFF00F0FF),
    QUANTUM_CRYSTALS("Quantum Shards", "💎", 0xFF9D4EDD),
    DARK_ENERGY("Dark Energy", "🔮", 0xFFFF007F),
    EXOTIC_ALLOY("Exotic Alloys", "🛡️", 0xFFFFB703),
    STELLAR_DATA("Stellar Cartography", "📜", 0xFF06D6A0)
}

data class SectorCoordinate(val row: Int, val col: Int) {
    val notation: String
        get() {
            val colChar = ('A'.code + col.coerceIn(0, 7)).toChar()
            val rowNum = 8 - row.coerceIn(0, 7)
            return "$colChar$rowNum"
        }

    val id: String get() = "${row}_${col}"

    companion object {
        fun fromNotation(str: String): SectorCoordinate? {
            if (str.length < 2) return null
            val col = str[0].uppercaseChar() - 'A'
            val row = 8 - (str.substring(1).toIntOrNull() ?: return null)
            if (col !in 0..7 || row !in 0..7) return null
            return SectorCoordinate(row, col)
        }
    }
}

data class Planet(
    val id: String,
    val name: String,
    val type: PlanetType,
    val orbitalRadius: Float, // Distance from star in simulator units
    val orbitSpeed: Float,    // Rad/s
    var currentAngle: Float,
    val radius: Float,        // Visual & collision radius
    val mass: Float,          // Gravitational mass
    val colorHex: Long,
    val hasRings: Boolean,
    val anomalyTitle: String?,
    val anomalyDesc: String?,
    val resource: ResourceType,
    val yield: Int,
    var isScanned: Boolean = false,
    var isSurveyed: Boolean = false
) {
    fun getPosition(center: Vector2D): Vector2D {
        return Vector2D(
            center.x + kotlin.math.cos(currentAngle) * orbitalRadius,
            center.y + kotlin.math.sin(currentAngle) * orbitalRadius
        )
    }
}

data class StarSystem(
    val id: String,
    val name: String,
    val coordinate: SectorCoordinate,
    val spectralClass: StarSpectralClass,
    val mass: Float,
    val radius: Float,
    val planets: List<Planet>,
    val asteroidBeltRadius: Float?,
    val sentinelThreatLevel: Int, // 1 to 5
    var isLiberated: Boolean = false,
    var hasMatrixMonolith: Boolean = true,
    var matrixPuzzleSolved: Boolean = false,
    val loreSnippet: String
)
