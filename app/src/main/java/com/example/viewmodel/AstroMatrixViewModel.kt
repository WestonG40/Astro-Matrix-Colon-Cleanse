package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.GalaxyGenerator
import com.example.engine.MatrixChessEngine
import com.example.model.AnomalyLog
import com.example.model.BoardCoordinate
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.GameResult
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.Planet
import com.example.model.QuantumGambit
import com.example.model.SectorCoordinate
import com.example.model.ShipComponent
import com.example.model.ShipFlightTelemetry
import com.example.model.ShipInventory
import com.example.model.StarSystem
import com.example.model.Vector2D
import com.example.physics.OrbitalPhysicsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ActiveScreen(val title: String) {
    ORBIT_SIMULATOR("Orbit Flight"),
    GALAXY_MAP("Galaxy Starchart"),
    MATRIX_CHESS("Matrix Chess"),
    SHIP_COMMAND("Ship & Codex")
}

enum class AutopilotMode(val label: String) {
    MANUAL("Manual RCS"),
    PROGRADE("Auto Prograde"),
    RETROGRADE("Auto Retrograde")
}

class AstroMatrixViewModel(application: Application) : AndroidViewModel(application) {

    // Galaxy Data
    private val _sectors = MutableStateFlow<Map<String, StarSystem>>(emptyMap())
    val sectors: StateFlow<Map<String, StarSystem>> = _sectors.asStateFlow()

    private val _currentSectorId = MutableStateFlow("4_4") // E4 (0-indexed row 4, col 4)
    val currentSectorId: StateFlow<String> = _currentSectorId.asStateFlow()

    private val _selectedSector = MutableStateFlow<StarSystem?>(null)
    val selectedSector: StateFlow<StarSystem?> = _selectedSector.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow(ActiveScreen.ORBIT_SIMULATOR)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    // Physics Engine & Telemetry
    private val physicsEngine = OrbitalPhysicsEngine()
    private val _telemetry = MutableStateFlow(ShipFlightTelemetry())
    val telemetry: StateFlow<ShipFlightTelemetry> = _telemetry.asStateFlow()

    private val _autopilot = MutableStateFlow(AutopilotMode.MANUAL)
    val autopilot: StateFlow<AutopilotMode> = _autopilot.asStateFlow()

    private val _timeWarp = MutableStateFlow(1f) // 1x, 2x, 5x
    val timeWarp: StateFlow<Float> = _timeWarp.asStateFlow()

    // Ship inventory & components
    private val _inventory = MutableStateFlow(ShipInventory())
    val inventory: StateFlow<ShipInventory> = _inventory.asStateFlow()

    private val _shipComponents = MutableStateFlow(
        listOf(
            ShipComponent("Fusion Thruster Drive", 1, 5, "Increases maximum thrust and fuel efficiency.", "+20% Delta-V", 30, 15),
            ShipComponent("Graviton Orbital Scanner", 1, 5, "Extends trajectory prediction and reveals hidden anomalies.", "+30% Survey Range", 25, 20),
            ShipComponent("Kinetic Shield Deflector", 1, 5, "Absorbs cosmic radiation and solar flares.", "+25% Shield Integrity", 40, 25),
            ShipComponent("Hyperlane Matrix Core", 1, 5, "Reduces hyperlane warp fuel costs between galaxy sectors.", "-20% Warp Cost", 50, 35)
        )
    )
    val shipComponents: StateFlow<List<ShipComponent>> = _shipComponents.asStateFlow()

    private val _anomalyLogs = MutableStateFlow<List<AnomalyLog>>(emptyList())
    val anomalyLogs: StateFlow<List<AnomalyLog>> = _anomalyLogs.asStateFlow()

    // Status toast/banner notification
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Matrix Chess Engine & State
    val chessEngine = MatrixChessEngine()
    private val _chessBoardVersion = MutableStateFlow(0)
    val chessBoardVersion: StateFlow<Int> = _chessBoardVersion.asStateFlow()

    private val _selectedChessSquare = MutableStateFlow<BoardCoordinate?>(null)
    val selectedChessSquare: StateFlow<BoardCoordinate?> = _selectedChessSquare.asStateFlow()

    private val _validChessMoves = MutableStateFlow<List<BoardCoordinate>>(emptyList())
    val validChessMoves: StateFlow<List<BoardCoordinate>> = _validChessMoves.asStateFlow()

    private val _activeGambit = MutableStateFlow<QuantumGambit?>(null)
    val activeGambit: StateFlow<QuantumGambit?> = _activeGambit.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(2) // 1=Cadet, 2=Sentinel, 3=Singularity
    val aiDifficulty: StateFlow<Int> = _aiDifficulty.asStateFlow()

    private val _isVsAi = MutableStateFlow(true)
    val isVsAi: StateFlow<Boolean> = _isVsAi.asStateFlow()

    // Vibrator
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        // Generate procedural galaxy
        val generated = GalaxyGenerator.generateGalaxy(seed = 987654321L)
        _sectors.value = generated
        // Initialize ship telemetry in circular orbit around starting star
        val startSystem = generated[_currentSectorId.value]
        _selectedSector.value = startSystem
        if (startSystem != null) {
            initCircularOrbit(startSystem)
        }

        // Launch real-time physics tick
        startPhysicsLoop()
    }

    fun setScreen(screen: ActiveScreen) {
        _currentScreen.value = screen
    }

    fun selectSector(system: StarSystem) {
        _selectedSector.value = system
    }

    fun jumpToSector(system: StarSystem) {
        val currentInv = _inventory.value
        val warpCost = 20

        if (currentInv.plasmaFuel < warpCost) {
            postMessage("Insufficient Plasma Fuel! Need $warpCost Fuel for hyperlane jump.")
            return
        }

        _inventory.update { it.copy(plasmaFuel = it.plasmaFuel - warpCost) }
        _currentSectorId.value = system.id
        _selectedSector.value = system

        initCircularOrbit(system)
        triggerHaptic(120)
        postMessage("Hyperlane Jump Engaged! Arrived in ${system.name} (${system.coordinate.notation}).")
    }

    private fun initCircularOrbit(system: StarSystem) {
        val orbitR = system.radius + 175f
        val orbitalSpeed = sqrt((physicsEngine.G * system.mass) / orbitR) * 0.98f

        _telemetry.update {
            it.copy(
                position = Vector2D(0f, -orbitR),
                velocity = Vector2D(orbitalSpeed, 0f),
                headingAngle = 0f,
                thrustPercent = 0f,
                fuel = 100f
            )
        }
    }

    fun setThrottle(percent: Float) {
        _telemetry.update { it.copy(thrustPercent = percent.coerceIn(0f, 1f)) }
        if (percent > 0.05f) {
            triggerHaptic(30)
        }
    }

    fun rotateShip(deltaRad: Float) {
        _telemetry.update {
            var newAngle = it.headingAngle + deltaRad
            if (newAngle > 2 * PI) newAngle -= (2 * PI).toFloat()
            if (newAngle < 0f) newAngle += (2 * PI).toFloat()
            it.copy(headingAngle = newAngle)
        }
        triggerHaptic(15)
    }

    fun setAutopilot(mode: AutopilotMode) {
        _autopilot.value = mode
        triggerHaptic(40)
        postMessage("Autopilot set to: ${mode.label}")
    }

    fun setTimeWarp(warp: Float) {
        _timeWarp.value = warp
        postMessage("Time Warp: ${warp.toInt()}x")
    }

    fun refuelFlightTank() {
        val inv = _inventory.value
        if (inv.plasmaFuel >= 25) {
            _inventory.update { it.copy(plasmaFuel = it.plasmaFuel - 25) }
            _telemetry.update { it.copy(fuel = 100f) }
            triggerHaptic(60)
            postMessage("Flight Fuel Tank Replenished to 100% (-25 Inventory Plasma).")
        } else {
            postMessage("Not enough inventory Plasma Fuel to refuel.")
        }
    }

    fun surveyNearestPlanet() {
        val system = _sectors.value[_currentSectorId.value] ?: return
        val currentTel = _telemetry.value

        // Check if close to any planet
        val nearestPlanet = system.planets.find { p ->
            val pPos = p.getPosition(Vector2D(0f, 0f))
            currentTel.position.distanceTo(pPos) <= p.radius + 60f
        }

        if (nearestPlanet == null) {
            postMessage("No planetary body within orbital survey range (< 60 units).")
            return
        }

        if (currentTel.currentSpeed > 5.5f) {
            postMessage("Relative speed too high for orbital survey (${String.format("%.1f", currentTel.currentSpeed)} km/s). Slow down with Retrograde burn!")
            return
        }

        if (nearestPlanet.isSurveyed) {
            postMessage("${nearestPlanet.name} has already been surveyed and cataloged.")
            return
        }

        nearestPlanet.isSurveyed = true
        nearestPlanet.isScanned = true

        // Award resources
        _inventory.update { inv ->
            when (nearestPlanet.resource) {
                com.example.model.ResourceType.PLASMA_FUEL -> inv.copy(plasmaFuel = (inv.plasmaFuel + nearestPlanet.yield).coerceAtMost(inv.maxFuel))
                com.example.model.ResourceType.QUANTUM_CRYSTALS -> inv.copy(quantumCrystals = inv.quantumCrystals + nearestPlanet.yield)
                com.example.model.ResourceType.DARK_ENERGY -> inv.copy(darkEnergy = inv.darkEnergy + nearestPlanet.yield)
                com.example.model.ResourceType.EXOTIC_ALLOY -> inv.copy(exoticAlloys = inv.exoticAlloys + nearestPlanet.yield)
                com.example.model.ResourceType.STELLAR_DATA -> inv.copy(stellarDataPoints = inv.stellarDataPoints + nearestPlanet.yield)
            }
        }

        if (nearestPlanet.anomalyTitle != null) {
            _anomalyLogs.update { logs ->
                listOf(
                    AnomalyLog(
                        id = nearestPlanet.id,
                        title = nearestPlanet.anomalyTitle,
                        sectorNotation = system.coordinate.notation,
                        description = nearestPlanet.anomalyDesc ?: ""
                    )
                ) + logs
            }
        }

        triggerHaptic(100)
        postMessage("Survey Complete! Discovered ${nearestPlanet.yield} ${nearestPlanet.resource.label} on ${nearestPlanet.name}!")
    }

    fun upgradeComponent(index: Int) {
        val list = _shipComponents.value.toMutableList()
        val comp = list.getOrNull(index) ?: return
        if (comp.level >= comp.maxLevel) {
            postMessage("${comp.name} is already at maximum Mk V rating!")
            return
        }

        val inv = _inventory.value
        if (inv.plasmaFuel >= comp.upgradeCostFuel && inv.quantumCrystals >= comp.upgradeCostCrystals) {
            _inventory.update {
                it.copy(
                    plasmaFuel = it.plasmaFuel - comp.upgradeCostFuel,
                    quantumCrystals = it.quantumCrystals - comp.upgradeCostCrystals
                )
            }
            list[index] = comp.copy(
                level = comp.level + 1,
                upgradeCostFuel = (comp.upgradeCostFuel * 1.5f).toInt(),
                upgradeCostCrystals = (comp.upgradeCostCrystals * 1.5f).toInt()
            )
            _shipComponents.value = list
            triggerHaptic(80)
            postMessage("${comp.name} upgraded to Mk ${romanMk(comp.level + 1)}!")
        } else {
            postMessage("Insufficient materials to upgrade! Need ${comp.upgradeCostFuel} Fuel & ${comp.upgradeCostCrystals} Crystals.")
        }
    }

    // Matrix Chess Actions
    fun onSquareClicked(coord: BoardCoordinate) {
        if (_isAiThinking.value) return

        val activeG = _activeGambit.value
        if (activeG != null) {
            // Apply gambit on clicked square
            val success = chessEngine.applyGambit(activeG, coord)
            _activeGambit.value = null
            if (success) {
                _chessBoardVersion.update { it + 1 }
                triggerHaptic(70)
                postMessage("${activeG.title} executed successfully!")
                if (chessEngine.currentTurn == PieceColor.BLACK && _isVsAi.value) {
                    triggerAiTurn()
                }
            } else {
                postMessage("Cannot apply ${activeG.title} on this coordinate.")
            }
            return
        }

        val selected = _selectedChessSquare.value
        if (selected == null) {
            // Select piece
            val piece = chessEngine.getPiece(coord)
            if (piece != null && piece.color == chessEngine.currentTurn) {
                _selectedChessSquare.value = coord
                _validChessMoves.value = chessEngine.getLegalMovesForPiece(coord)
                triggerHaptic(25)
            }
        } else {
            if (coord == selected) {
                // Deselect
                _selectedChessSquare.value = null
                _validChessMoves.value = emptyList()
            } else if (coord in _validChessMoves.value) {
                // Execute move
                val moved = chessEngine.makeMove(selected, coord)
                _selectedChessSquare.value = null
                _validChessMoves.value = emptyList()
                _chessBoardVersion.update { it + 1 }

                if (moved) {
                    triggerHaptic(60)
                    checkDuelVictory()

                    if (chessEngine.currentTurn == PieceColor.BLACK && _isVsAi.value && chessEngine.gameResult == GameResult.ONGOING) {
                        triggerAiTurn()
                    }
                }
            } else {
                // Check if selecting another friendly piece
                val piece = chessEngine.getPiece(coord)
                if (piece != null && piece.color == chessEngine.currentTurn) {
                    _selectedChessSquare.value = coord
                    _validChessMoves.value = chessEngine.getLegalMovesForPiece(coord)
                    triggerHaptic(25)
                } else {
                    _selectedChessSquare.value = null
                    _validChessMoves.value = emptyList()
                }
            }
        }
    }

    fun selectGambit(gambit: QuantumGambit) {
        val energy = if (chessEngine.currentTurn == PieceColor.WHITE) chessEngine.whiteEnergy else chessEngine.blackEnergy
        if (energy < gambit.cost) {
            postMessage("Insufficient Quantum Energy! Requires ${gambit.cost} energy.")
            return
        }

        if (_activeGambit.value == gambit) {
            _activeGambit.value = null
        } else {
            _activeGambit.value = gambit
            postMessage("Select target coordinate on the matrix board for ${gambit.title}.")
        }
    }

    fun resetChessBoard() {
        chessEngine.resetBoard()
        _selectedChessSquare.value = null
        _validChessMoves.value = emptyList()
        _activeGambit.value = null
        _chessBoardVersion.update { it + 1 }
        postMessage("Matrix Chess battlefield recalibrated.")
    }

    fun setAiDifficulty(diff: Int) {
        _aiDifficulty.value = diff
        postMessage("Sentinel AI protocol set to: ${when (diff) { 1 -> "Cadet Drone"; 2 -> "Sentinel AI"; else -> "Singularity Grandmaster" }}")
    }

    fun setVsAi(vsAi: Boolean) {
        _isVsAi.value = vsAi
        postMessage(if (vsAi) "Tactical combat set to: Explorer vs Sentinel AI" else "Local Pass-and-Play 2-Commander Mode")
    }

    private fun triggerAiTurn() {
        _isAiThinking.value = true
        viewModelScope.launch {
            delay(500) // Realistic computational cadence
            val bestMove = withContext(Dispatchers.Default) {
                chessEngine.computeBestAiMove(_aiDifficulty.value)
            }
            if (bestMove != null) {
                chessEngine.makeMove(bestMove.first, bestMove.second)
                _chessBoardVersion.update { it + 1 }
                triggerHaptic(50)
                checkDuelVictory()
            }
            _isAiThinking.value = false
        }
    }

    private fun checkDuelVictory() {
        if (chessEngine.gameResult == GameResult.WHITE_VICTORY) {
            val currSector = _sectors.value[_currentSectorId.value]
            if (currSector != null && !currSector.isLiberated) {
                currSector.isLiberated = true
                currSector.matrixPuzzleSolved = true
                _inventory.update {
                    it.copy(
                        plasmaFuel = (it.plasmaFuel + 50).coerceAtMost(it.maxFuel),
                        quantumCrystals = it.quantumCrystals + 35,
                        darkEnergy = it.darkEnergy + 20,
                        stellarDataPoints = it.stellarDataPoints + 30
                    )
                }
                triggerHaptic(200)
                postMessage("🌟 VICTORY! Sector ${currSector.coordinate.notation} Liberated from Sentinel Blockade! Received +50 Fuel, +35 Crystals, +20 Dark Energy!")
            }
        }
    }

    private fun startPhysicsLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            val dt = 0.04f // 25 fps physics simulation
            while (isActive) {
                val system = _sectors.value[_currentSectorId.value]
                if (system != null) {
                    val effectiveDt = dt * _timeWarp.value
                    physicsEngine.updatePlanets(system.planets, effectiveDt)

                    val auto = _autopilot.value
                    physicsEngine.stepPhysics(
                        telemetry = _telemetry.value,
                        starSystem = system,
                        dt = effectiveDt,
                        autoPrograde = auto == AutopilotMode.PROGRADE,
                        autoRetrograde = auto == AutopilotMode.RETROGRADE
                    )
                    // Trigger state update
                    _telemetry.update { it.copy() }
                }
                delay(40)
            }
        }
    }

    fun triggerHaptic(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun postMessage(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    private fun romanMk(num: Int): String {
        return when (num) {
            1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"; else -> "$num"
        }
    }
}
