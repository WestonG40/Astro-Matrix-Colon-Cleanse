package com.example.model

enum class PieceColor(val label: String) {
    WHITE("Explorer Fleet"),
    BLACK("Sentinel Matrix")
}

enum class PieceType(
    val symbol: String,
    val sciFiTitle: String,
    val value: Int
) {
    PAWN("♙", "Drone Scout", 100),
    KNIGHT("♘", "Subspace Raider", 320),
    BISHOP("♗", "Tachyon Skiff", 330),
    ROOK("♖", "Railgun Fortress", 500),
    QUEEN("♕", "Singularity Battlecruiser", 900),
    KING("♔", "Flagship Dreadnought", 20000)
}

enum class CosmicHazard(
    val title: String,
    val description: String,
    val icon: String
) {
    NONE("Normal Space", "Standard matrix coordinates.", ""),
    GRAVITY_WELL("Gravity Singularity", "Central anomaly that pulls light and particles. Moving into this node grants +15 Quantum Energy.", "🌀"),
    WORMHOLE_ALPHA("Wormhole Alpha", "Quantum gate linked to Wormhole Beta. Stepping here allows hyperlane transit across the board!", "🌌"),
    WORMHOLE_BETA("Wormhole Beta", "Quantum gate linked to Wormhole Alpha. Stepping here allows hyperlane transit across the board!", "🌌"),
    NEBULA_FIELD("Plasma Nebula", "Shields pieces against long-range scanning; grants +10 Quantum Energy.", "✨")
}

data class BoardCoordinate(val row: Int, val col: Int) {
    val notation: String
        get() {
            val file = ('a'.code + col.coerceIn(0, 7)).toChar()
            val rank = 8 - row.coerceIn(0, 7)
            return "$file$rank"
        }

    fun isValid(): Boolean = row in 0..7 && col in 0..7

    companion object {
        fun fromNotation(not: String): BoardCoordinate? {
            if (not.length < 2) return null
            val col = not[0].lowercaseChar() - 'a'
            val row = 8 - (not.substring(1).toIntOrNull() ?: return null)
            if (col !in 0..7 || row !in 0..7) return null
            return BoardCoordinate(row, col)
        }
    }
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val id: String = "${color}_${type}_${java.util.UUID.randomUUID().toString().take(6)}",
    val hasMoved: Boolean = false,
    val shieldTurnsRemaining: Int = 0
)

data class ChessMove(
    val from: BoardCoordinate,
    val to: BoardCoordinate,
    val piece: ChessPiece,
    val captured: ChessPiece? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false,
    val promotionType: PieceType? = null,
    val usedWormhole: Boolean = false,
    val notation: String = ""
)

enum class GameResult(val message: String) {
    ONGOING("Game in progress"),
    WHITE_VICTORY("Explorer Fleet Checkmate Victory! Sector Secured!"),
    BLACK_VICTORY("Sentinel Matrix Checkmate! Tactical Retreat!"),
    STALEMATE("Quantum Stalemate - Sector Matrix Stable"),
    DRAW_INSUFFICIENT("Draw - Insufficient Fleet Material")
}

enum class QuantumGambit(
    val title: String,
    val cost: Int,
    val desc: String,
    val icon: String
) {
    GRAVITON_PULSE(
        title = "Graviton Pulse",
        cost = 20,
        desc = "Emit high-energy wave to push an adjacent enemy piece 1 square backward.",
        icon = "💥"
    ),
    HYPERDRIVE_LEAP(
        title = "Hyperdrive Leap",
        cost = 30,
        desc = "Quantum phase-shift a friendly piece to any adjacent empty tile, ignoring barriers.",
        icon = "⚡"
    ),
    TEMPORAL_SHIELD(
        title = "Temporal Shield",
        cost = 25,
        desc = "Project a defensive stasis field on a friendly piece preventing capture for 2 turns.",
        icon = "🛡️"
    )
}
