package com.example.engine

import com.example.model.Planet
import com.example.model.PlanetType
import com.example.model.ResourceType
import com.example.model.SectorCoordinate
import com.example.model.StarSpectralClass
import com.example.model.StarSystem
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

object GalaxyGenerator {

    private val STAR_NAMES = listOf(
        "Aethelgard", "Vorticon", "Hyperion", "Chronos", "Zeta Reticuli",
        "Altair Prime", "Kepler-452", "Vega Minor", "Sirius Nexus", "Polaris Gate",
        "Epsilon Eridani", "Betelgeuse Drift", "Antares Reach", "Rigel Abyss", "Deneb Void",
        "Aldebaran", "Fomalhaut", "Spica Helix", "Arcturus Spire", "Procyon",
        "Canopus", "Bellatrix", "Castor Matrix", "Pollux Core", "Mirfak",
        "Achernar", "Alkaid", "Regulus", "Mizar Array", "Capella",
        "Titanis", "Oberon Verge", "Cygnus X-1", "Sagittarius Prime", "Novus",
        "Astraea", "Tesseract Prime", "Oasis Matrix", "Elysium Core", "Styx Deep"
    )

    private val PLANET_PREFIXES = listOf(
        "Aethel", "Kryos", "Vesper", "Astra", "Zephyr", "Solaria", "Thalassa",
        "Obsidian", "Ignis", "Chronos", "Xylar", "Pyre", "Boreas", "Nyx", "Helios"
    )

    private val ANOMALY_TITLES = listOf(
        "Matrix Monolith Inscription",
        "Tachyon Resonance Beacon",
        "Crystalline Dyson Fragment",
        "Subspace Graviton Well",
        "Ancient Chess Automaton",
        "Quantum Singularity Echo",
        "Derelict Sentinel Dreadnought"
    )

    private val ANOMALY_DESCS = listOf(
        "A gigantic crystalline obelisk transmitting tactical matrix chess sequences into the stellar slipstream.",
        "An ancient navigational beacon vibrating at harmonic quantum frequencies, revealing local hyperlanes.",
        "Orbiting megastructure remnants from an ancient civilization who encoded galaxy coordinates into chess matrices.",
        "Localized spacetime curvature twisting light into geometric chessboard grids across the planetary surface.",
        "Dormant autonomous guardian programmed to challenge interstellar explorers in grandmaster tactical duels.",
        "Zero-point energy rift pulsating in synchronized rhythm with cosmic dark matter flows.",
        "Hulking alien flagship floating in perpetual orbit, guarded by sentinel defense drones."
    )

    fun generateGalaxy(seed: Long = 133742L): Map<String, StarSystem> {
        val random = Random(seed)
        val sectors = mutableMapOf<String, StarSystem>()

        var nameIdx = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val coord = SectorCoordinate(r, c)
                val distFromCenter = sqrt((r - 3.5f) * (r - 3.5f) + (c - 3.5f) * (c - 3.5f))

                // Center (D4, D5, E4, E5) has high chance of Black Hole or Pulsar
                val spectralClass = when {
                    distFromCenter < 1.2f -> if (random.nextBoolean()) StarSpectralClass.BLACK_HOLE else StarSpectralClass.PULSAR
                    distFromCenter < 2.5f -> {
                        val roll = random.nextFloat()
                        when {
                            roll < 0.35f -> StarSpectralClass.BLUE_HYPERGIANT
                            roll < 0.70f -> StarSpectralClass.SOL_DWARF
                            else -> StarSpectralClass.WHITE_DWARF
                        }
                    }
                    else -> {
                        val roll = random.nextFloat()
                        when {
                            roll < 0.35f -> StarSpectralClass.SOL_DWARF
                            roll < 0.65f -> StarSpectralClass.RED_GIANT
                            roll < 0.85f -> StarSpectralClass.WHITE_DWARF
                            else -> StarSpectralClass.BLUE_HYPERGIANT
                        }
                    }
                }

                val starName = if (distFromCenter < 0.9f) {
                    "Sagittarius Singularity ${coord.notation}"
                } else {
                    "${STAR_NAMES[(nameIdx++) % STAR_NAMES.size]} ${coord.notation}"
                }

                // Star Mass & Radius
                val starMass = spectralClass.baseMass * (0.85f + random.nextFloat() * 0.3f)
                val starRadius = when (spectralClass) {
                    StarSpectralClass.BLACK_HOLE -> 24f
                    StarSpectralClass.PULSAR -> 18f
                    StarSpectralClass.BLUE_HYPERGIANT -> 42f
                    StarSpectralClass.RED_GIANT -> 38f
                    StarSpectralClass.SOL_DWARF -> 30f
                    StarSpectralClass.WHITE_DWARF -> 20f
                }

                // Generate 2 to 4 planets
                val numPlanets = if (spectralClass == StarSpectralClass.BLACK_HOLE) {
                    random.nextInt(1, 3) // Hostile accretion environment
                } else {
                    random.nextInt(2, 5)
                }

                val planets = mutableListOf<Planet>()
                var currentDist = starRadius + 75f + random.nextFloat() * 25f

                for (p in 0 until numPlanets) {
                    val pType = when {
                        spectralClass == StarSpectralClass.BLACK_HOLE -> PlanetType.FORGOTTEN_RUIN
                        currentDist < 140f -> if (random.nextBoolean()) PlanetType.LAVA_WORLD else PlanetType.TERRESTRIAL
                        currentDist < 260f -> {
                            val pRoll = random.nextFloat()
                            when {
                                pRoll < 0.4f -> PlanetType.OCEAN_WORLD
                                pRoll < 0.7f -> PlanetType.TERRESTRIAL
                                else -> PlanetType.MATRIX_CORE
                            }
                        }
                        currentDist < 360f -> PlanetType.GAS_GIANT
                        else -> PlanetType.ICE_WORLD
                    }

                    val planetRadius = when (pType) {
                        PlanetType.GAS_GIANT -> 18f + random.nextFloat() * 8f
                        PlanetType.MATRIX_CORE -> 14f + random.nextFloat() * 4f
                        else -> 10f + random.nextFloat() * 5f
                    }

                    val planetMass = planetRadius * 60f + random.nextFloat() * 200f
                    // Kepler's third law: v proportional to 1/sqrt(r)
                    val baseSpeed = sqrt(starMass / (currentDist * 1.5f)) * 0.005f
                    val orbitSpeed = (if (random.nextBoolean()) baseSpeed else -baseSpeed).coerceIn(-0.015f, 0.015f)

                    val resource = when (pType) {
                        PlanetType.MATRIX_CORE -> ResourceType.QUANTUM_CRYSTALS
                        PlanetType.GAS_GIANT -> ResourceType.PLASMA_FUEL
                        PlanetType.FORGOTTEN_RUIN -> ResourceType.DARK_ENERGY
                        PlanetType.LAVA_WORLD -> ResourceType.EXOTIC_ALLOY
                        else -> if (random.nextBoolean()) ResourceType.STELLAR_DATA else ResourceType.PLASMA_FUEL
                    }

                    val hasAnomaly = random.nextFloat() < 0.55f || pType == PlanetType.MATRIX_CORE || pType == PlanetType.FORGOTTEN_RUIN
                    val anomIdx = random.nextInt(ANOMALY_TITLES.size)

                    val planetName = "${PLANET_PREFIXES[random.nextInt(PLANET_PREFIXES.size)]} ${romanNumeral(p + 1)}"

                    planets.add(
                        Planet(
                            id = "${coord.id}_p$p",
                            name = planetName,
                            type = pType,
                            orbitalRadius = currentDist,
                            orbitSpeed = if (orbitSpeed == 0f) 0.004f else orbitSpeed,
                            currentAngle = random.nextFloat() * (2 * PI.toFloat()),
                            radius = planetRadius,
                            mass = planetMass,
                            colorHex = pType.baseColorHex,
                            hasRings = pType == PlanetType.GAS_GIANT && random.nextBoolean(),
                            anomalyTitle = if (hasAnomaly) ANOMALY_TITLES[anomIdx] else null,
                            anomalyDesc = if (hasAnomaly) ANOMALY_DESCS[anomIdx] else null,
                            resource = resource,
                            yield = random.nextInt(15, 45)
                        )
                    )

                    currentDist += 75f + random.nextFloat() * 55f
                }

                val threat = ((distFromCenter * 0.8f).toInt() + random.nextInt(1, 3)).coerceIn(1, 5)

                val lore = when (spectralClass) {
                    StarSpectralClass.BLACK_HOLE -> "A deep gravitational singularity emitting quantum resonance patterns. Matrix signals originate from within the event horizon."
                    StarSpectralClass.PULSAR -> "A dense neutron star spinning at thousands of RPM, acting as a galactic lighthouse for interstellar matrix navigators."
                    StarSpectralClass.BLUE_HYPERGIANT -> "A luminous titan burning through nuclear reserves, bathing surrounding space in radiant stellar plasma."
                    StarSpectralClass.SOL_DWARF -> "A calm, stable star harboring rich planetary bodies with active matrix ruins and alien monoliths."
                    StarSpectralClass.WHITE_DWARF -> "The dense remnant core of a dead star, surrounded by cold crystalline planetoids."
                    StarSpectralClass.RED_GIANT -> "An ancient dying star expanding outward, its coronal loops grazing the inner planetary orbits."
                }

                val hasBelt = random.nextFloat() < 0.4f
                val beltRadius = if (hasBelt) currentDist - 40f else null

                // Starting sector (E4) is liberated and has low threat
                val isStart = coord.notation == "E4"

                val system = StarSystem(
                    id = coord.id,
                    name = starName,
                    coordinate = coord,
                    spectralClass = spectralClass,
                    mass = starMass,
                    radius = starRadius,
                    planets = planets,
                    asteroidBeltRadius = beltRadius,
                    sentinelThreatLevel = if (isStart) 1 else threat,
                    isLiberated = isStart,
                    hasMatrixMonolith = true,
                    matrixPuzzleSolved = isStart,
                    loreSnippet = lore
                )

                sectors[coord.id] = system
            }
        }

        return sectors
    }

    private fun romanNumeral(num: Int): String {
        return when (num) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            else -> "$num"
        }
    }
}
