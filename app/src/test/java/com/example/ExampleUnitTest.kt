package com.example

import com.example.engine.GalaxyGenerator
import com.example.engine.MatrixChessEngine
import com.example.model.BoardCoordinate
import com.example.model.CosmicHazard
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.SectorCoordinate
import com.example.model.ShipFlightTelemetry
import com.example.model.Vector2D
import com.example.physics.OrbitalPhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testGalaxyGeneration() {
        val galaxy = GalaxyGenerator.generateGalaxy(seed = 12345L)
        assertEquals(64, galaxy.size)

        // Check start sector E4 exists
        val e4 = galaxy["4_4"]
        assertNotNull(e4)
        assertEquals("E4", e4?.coordinate?.notation)
        assertTrue(e4?.planets?.isNotEmpty() == true)

        // Check A1 exists
        val a1 = galaxy["7_0"]
        assertNotNull(a1)
        assertEquals("A1", a1?.coordinate?.notation)
    }

    @Test
    fun testOrbitalPhysics() {
        val galaxy = GalaxyGenerator.generateGalaxy(seed = 12345L)
        val system = galaxy["3_4"]!!
        val engine = OrbitalPhysicsEngine()

        val telemetry = ShipFlightTelemetry(
            position = Vector2D(0f, -200f),
            velocity = Vector2D(3f, 0f),
            thrustPercent = 0.5f,
            fuel = 100f
        )

        // Step physics forward
        engine.stepPhysics(telemetry, system, dt = 0.05f)

        // Position and speed must update
        assertTrue(telemetry.position.x != 0f)
        assertTrue(telemetry.currentSpeed > 0f)
        assertTrue(telemetry.fuel < 100f)
        assertTrue(telemetry.trajectoryPoints.isNotEmpty())
    }

    @Test
    fun testMatrixChessEngine() {
        val chess = MatrixChessEngine()
        assertEquals(PieceColor.WHITE, chess.currentTurn)

        // White pawn at e2 (row 6, col 4) has moves to e3 and e4
        val e2 = BoardCoordinate(6, 4)
        val legalMoves = chess.getLegalMovesForPiece(e2)
        assertTrue(legalMoves.contains(BoardCoordinate(5, 4)))
        assertTrue(legalMoves.contains(BoardCoordinate(4, 4)))

        // Move pawn e2 to e4 (which is a center Gravity Well!)
        val initialEnergy = chess.whiteEnergy
        val moved = chess.makeMove(e2, BoardCoordinate(4, 4))
        assertTrue(moved)
        assertEquals(PieceColor.BLACK, chess.currentTurn)

        // Gravity well grants quantum energy bonus
        assertTrue(chess.whiteEnergy >= initialEnergy)
    }
}
