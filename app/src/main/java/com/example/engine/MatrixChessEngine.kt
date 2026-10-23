package com.example.engine

import com.example.model.BoardCoordinate
import com.example.model.ChessMove
import com.example.model.ChessPiece
import com.example.model.CosmicHazard
import com.example.model.GameResult
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.QuantumGambit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MatrixChessEngine {

    val board = mutableMapOf<BoardCoordinate, ChessPiece>()
    var currentTurn = PieceColor.WHITE
    var whiteEnergy = 40
    var blackEnergy = 40
    val moveHistory = mutableListOf<ChessMove>()
    val capturedByWhite = mutableListOf<ChessPiece>()
    val capturedByBlack = mutableListOf<ChessPiece>()
    var gameResult = GameResult.ONGOING

    // Cosmic hazards mapping
    val hazards = mutableMapOf<BoardCoordinate, CosmicHazard>()

    init {
        setupHazards()
        resetBoard()
    }

    private fun setupHazards() {
        hazards.clear()
        // Center Gravity Wells
        hazards[BoardCoordinate(3, 3)] = CosmicHazard.GRAVITY_WELL // d5
        hazards[BoardCoordinate(3, 4)] = CosmicHazard.GRAVITY_WELL // e5
        hazards[BoardCoordinate(4, 3)] = CosmicHazard.GRAVITY_WELL // d4
        hazards[BoardCoordinate(4, 4)] = CosmicHazard.GRAVITY_WELL // e4

        // Wormhole Pair
        hazards[BoardCoordinate(3, 0)] = CosmicHazard.WORMHOLE_ALPHA // a5
        hazards[BoardCoordinate(4, 7)] = CosmicHazard.WORMHOLE_BETA  // h4

        // Plasma Nebulae
        hazards[BoardCoordinate(2, 2)] = CosmicHazard.NEBULA_FIELD
        hazards[BoardCoordinate(5, 5)] = CosmicHazard.NEBULA_FIELD
    }

    fun resetBoard() {
        board.clear()
        moveHistory.clear()
        capturedByWhite.clear()
        capturedByBlack.clear()
        currentTurn = PieceColor.WHITE
        whiteEnergy = 40
        blackEnergy = 40
        gameResult = GameResult.ONGOING

        // Setup Black pieces (row 0 & 1)
        board[BoardCoordinate(0, 0)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        board[BoardCoordinate(0, 1)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[BoardCoordinate(0, 2)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[BoardCoordinate(0, 3)] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
        board[BoardCoordinate(0, 4)] = ChessPiece(PieceType.KING, PieceColor.BLACK)
        board[BoardCoordinate(0, 5)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
        board[BoardCoordinate(0, 6)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
        board[BoardCoordinate(0, 7)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
        for (c in 0..7) {
            board[BoardCoordinate(1, c)] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
        }

        // Setup White pieces (row 6 & 7)
        for (c in 0..7) {
            board[BoardCoordinate(6, c)] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        }
        board[BoardCoordinate(7, 0)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
        board[BoardCoordinate(7, 1)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[BoardCoordinate(7, 2)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[BoardCoordinate(7, 3)] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
        board[BoardCoordinate(7, 4)] = ChessPiece(PieceType.KING, PieceColor.WHITE)
        board[BoardCoordinate(7, 5)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
        board[BoardCoordinate(7, 6)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
        board[BoardCoordinate(7, 7)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
    }

    fun getPiece(coord: BoardCoordinate): ChessPiece? = board[coord]

    fun getHazard(coord: BoardCoordinate): CosmicHazard = hazards[coord] ?: CosmicHazard.NONE

    fun getLegalMovesForPiece(coord: BoardCoordinate): List<BoardCoordinate> {
        val piece = board[coord] ?: return emptyList()
        if (piece.color != currentTurn) return emptyList()

        val candidateMoves = getCandidateMoves(coord, piece)
        return candidateMoves.filter { toCoord ->
            isMoveLegal(coord, toCoord, piece)
        }
    }

    private fun getCandidateMoves(
        coord: BoardCoordinate,
        piece: ChessPiece,
        includeCastling: Boolean = true
    ): List<BoardCoordinate> {
        val moves = mutableListOf<BoardCoordinate>()
        val (r, c) = coord

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1

                // 1 forward
                val oneFwd = BoardCoordinate(r + dir, c)
                if (oneFwd.isValid() && board[oneFwd] == null) {
                    moves.add(oneFwd)
                    // 2 forward
                    val twoFwd = BoardCoordinate(r + dir * 2, c)
                    if (r == startRow && twoFwd.isValid() && board[twoFwd] == null) {
                        moves.add(twoFwd)
                    }
                }

                // Diagonal captures
                for (dc in listOf(-1, 1)) {
                    val diag = BoardCoordinate(r + dir, c + dc)
                    if (diag.isValid()) {
                        val target = board[diag]
                        if (target != null && target.color != piece.color && target.shieldTurnsRemaining <= 0) {
                            moves.add(diag)
                        }
                    }
                }
            }

            PieceType.KNIGHT -> {
                val knightOffsets = listOf(
                    -2 to -1, -2 to 1, -1 to -2, -1 to 2,
                    1 to -2, 1 to 2, 2 to -1, 2 to 1
                )
                for ((dr, dc) in knightOffsets) {
                    val target = BoardCoordinate(r + dr, c + dc)
                    if (target.isValid()) {
                        val dest = board[target]
                        if (dest == null || (dest.color != piece.color && dest.shieldTurnsRemaining <= 0)) {
                            moves.add(target)
                        }
                    }
                }
            }

            PieceType.BISHOP -> {
                addRays(coord, piece, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1), moves)
            }

            PieceType.ROOK -> {
                addRays(coord, piece, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1), moves)
            }

            PieceType.QUEEN -> {
                addRays(
                    coord, piece,
                    listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1),
                    moves
                )
            }

            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val target = BoardCoordinate(r + dr, c + dc)
                        if (target.isValid()) {
                            val dest = board[target]
                            if (dest == null || (dest.color != piece.color && dest.shieldTurnsRemaining <= 0)) {
                                moves.add(target)
                            }
                        }
                    }
                }

                // Castling only when requested and not already in check
                if (includeCastling && !piece.hasMoved && !isKingInCheck(piece.color)) {
                    // Kingside
                    val kRookCoord = BoardCoordinate(r, 7)
                    val kRook = board[kRookCoord]
                    if (kRook?.type == PieceType.ROOK && !kRook.hasMoved &&
                        board[BoardCoordinate(r, 5)] == null && board[BoardCoordinate(r, 6)] == null
                    ) {
                        if (!isSquareAttacked(BoardCoordinate(r, 5), piece.color.opposite()) &&
                            !isSquareAttacked(BoardCoordinate(r, 6), piece.color.opposite())
                        ) {
                            moves.add(BoardCoordinate(r, 6))
                        }
                    }
                    // Queenside
                    val qRookCoord = BoardCoordinate(r, 0)
                    val qRook = board[qRookCoord]
                    if (qRook?.type == PieceType.ROOK && !qRook.hasMoved &&
                        board[BoardCoordinate(r, 1)] == null &&
                        board[BoardCoordinate(r, 2)] == null &&
                        board[BoardCoordinate(r, 3)] == null
                    ) {
                        if (!isSquareAttacked(BoardCoordinate(r, 2), piece.color.opposite()) &&
                            !isSquareAttacked(BoardCoordinate(r, 3), piece.color.opposite())
                        ) {
                            moves.add(BoardCoordinate(r, 2))
                        }
                    }
                }
            }
        }

        // Wormhole transit option: If currently standing on Wormhole Alpha, can jump to Wormhole Beta (and vice versa)
        val currentHazard = hazards[coord]
        if (currentHazard == CosmicHazard.WORMHOLE_ALPHA) {
            val betaCoord = BoardCoordinate(4, 7)
            val dest = board[betaCoord]
            if (dest == null || (dest.color != piece.color && dest.shieldTurnsRemaining <= 0)) {
                moves.add(betaCoord)
            }
        } else if (currentHazard == CosmicHazard.WORMHOLE_BETA) {
            val alphaCoord = BoardCoordinate(3, 0)
            val dest = board[alphaCoord]
            if (dest == null || (dest.color != piece.color && dest.shieldTurnsRemaining <= 0)) {
                moves.add(alphaCoord)
            }
        }

        return moves
    }

    private fun addRays(
        from: BoardCoordinate,
        piece: ChessPiece,
        directions: List<Pair<Int, Int>>,
        outMoves: MutableList<BoardCoordinate>
    ) {
        for ((dr, dc) in directions) {
            var curR = from.row + dr
            var curC = from.col + dc
            while (curR in 0..7 && curC in 0..7) {
                val target = BoardCoordinate(curR, curC)
                val dest = board[target]
                if (dest == null) {
                    outMoves.add(target)
                } else {
                    if (dest.color != piece.color && dest.shieldTurnsRemaining <= 0) {
                        outMoves.add(target)
                    }
                    break // Hit obstacle
                }
                curR += dr
                curC += dc
            }
        }
    }

    private fun isMoveLegal(from: BoardCoordinate, to: BoardCoordinate, piece: ChessPiece): Boolean {
        // Try move on temporary board
        val captured = board[to]
        board.remove(from)
        board[to] = piece

        val inCheck = isKingInCheck(piece.color)

        // Undo move
        board[from] = piece
        if (captured != null) {
            board[to] = captured
        } else {
            board.remove(to)
        }

        return !inCheck
    }

    fun makeMove(from: BoardCoordinate, to: BoardCoordinate, promotionType: PieceType = PieceType.QUEEN): Boolean {
        if (gameResult != GameResult.ONGOING) return false
        val piece = board[from] ?: return false
        if (piece.color != currentTurn) return false

        val legalMoves = getLegalMovesForPiece(from)
        if (to !in legalMoves) return false

        // Check for castling
        val isCastling = piece.type == PieceType.KING && abs(from.col - to.col) == 2
        if (isCastling) {
            val r = from.row
            if (to.col == 6) { // Kingside
                val rook = board.remove(BoardCoordinate(r, 7))
                if (rook != null) {
                    board[BoardCoordinate(r, 5)] = rook.copy(hasMoved = true)
                }
            } else if (to.col == 2) { // Queenside
                val rook = board.remove(BoardCoordinate(r, 0))
                if (rook != null) {
                    board[BoardCoordinate(r, 3)] = rook.copy(hasMoved = true)
                }
            }
        }

        val captured = board[to]
        if (captured != null) {
            if (currentTurn == PieceColor.WHITE) {
                capturedByWhite.add(captured)
                whiteEnergy = min(100, whiteEnergy + 15)
            } else {
                capturedByBlack.add(captured)
                blackEnergy = min(100, blackEnergy + 15)
            }
        }

        // Check hazard on arrival square
        val destHazard = hazards[to]
        when (destHazard) {
            CosmicHazard.GRAVITY_WELL -> {
                if (currentTurn == PieceColor.WHITE) whiteEnergy = min(100, whiteEnergy + 12)
                else blackEnergy = min(100, blackEnergy + 12)
            }
            CosmicHazard.NEBULA_FIELD -> {
                if (currentTurn == PieceColor.WHITE) whiteEnergy = min(100, whiteEnergy + 8)
                else blackEnergy = min(100, blackEnergy + 8)
            }
            else -> {}
        }

        // Check pawn promotion
        val isPromo = piece.type == PieceType.PAWN && ((piece.color == PieceColor.WHITE && to.row == 0) || (piece.color == PieceColor.BLACK && to.row == 7))
        val movedPiece = if (isPromo) {
            piece.copy(type = promotionType, hasMoved = true)
        } else {
            piece.copy(hasMoved = true)
        }

        board.remove(from)
        board[to] = movedPiece

        // Decay shields for current turn's pieces
        val shieldedEntries = board.filter { it.value.color == currentTurn && it.value.shieldTurnsRemaining > 0 }
        for ((coord, p) in shieldedEntries) {
            board[coord] = p.copy(shieldTurnsRemaining = p.shieldTurnsRemaining - 1)
        }

        // Record move
        val notStr = "${piece.type.symbol}${from.notation}-${to.notation}"
        moveHistory.add(ChessMove(from, to, piece, captured, isCastling = isCastling, promotionType = if (isPromo) promotionType else null, notation = notStr))

        // Switch turn
        currentTurn = currentTurn.opposite()

        // Check game end conditions
        updateGameResult()

        return true
    }

    fun applyGambit(gambit: QuantumGambit, targetCoord: BoardCoordinate): Boolean {
        val energy = if (currentTurn == PieceColor.WHITE) whiteEnergy else blackEnergy
        if (energy < gambit.cost) return false

        when (gambit) {
            QuantumGambit.GRAVITON_PULSE -> {
                val targetPiece = board[targetCoord] ?: return false
                if (targetPiece.color == currentTurn) return false
                // Push backward along rank/file
                val pushDir = if (currentTurn == PieceColor.WHITE) -1 else 1
                val destCoord = BoardCoordinate(targetCoord.row + pushDir, targetCoord.col)
                if (destCoord.isValid() && board[destCoord] == null) {
                    board.remove(targetCoord)
                    board[destCoord] = targetPiece
                } else return false
            }

            QuantumGambit.HYPERDRIVE_LEAP -> {
                val targetPiece = board[targetCoord] ?: return false
                if (targetPiece.color != currentTurn) return false
                // Find first available adjacent square
                var warped = false
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val adj = BoardCoordinate(targetCoord.row + dr, targetCoord.col + dc)
                        if (adj.isValid() && board[adj] == null) {
                            board.remove(targetCoord)
                            board[adj] = targetPiece
                            warped = true
                            break
                        }
                    }
                    if (warped) break
                }
                if (!warped) return false
            }

            QuantumGambit.TEMPORAL_SHIELD -> {
                val targetPiece = board[targetCoord] ?: return false
                if (targetPiece.color != currentTurn) return false
                board[targetCoord] = targetPiece.copy(shieldTurnsRemaining = 2)
            }
        }

        if (currentTurn == PieceColor.WHITE) whiteEnergy -= gambit.cost
        else blackEnergy -= gambit.cost

        return true
    }

    private fun isKingInCheck(color: PieceColor): Boolean {
        val kingEntry = board.entries.find { it.value.type == PieceType.KING && it.value.color == color } ?: return false
        return isSquareAttacked(kingEntry.key, color.opposite())
    }

    private fun isSquareAttacked(square: BoardCoordinate, byColor: PieceColor): Boolean {
        for ((coord, piece) in board.toList()) {
            if (piece.color == byColor) {
                // If pawn, special attack diagonals
                if (piece.type == PieceType.PAWN) {
                    val dir = if (byColor == PieceColor.WHITE) -1 else 1
                    if (square.row == coord.row + dir && (square.col == coord.col - 1 || square.col == coord.col + 1)) {
                        return true
                    }
                } else {
                    val rawMoves = getCandidateMoves(coord, piece, includeCastling = false)
                    if (square in rawMoves) return true
                }
            }
        }
        return false
    }

    private fun updateGameResult() {
        val allLegalMoves = mutableListOf<Pair<BoardCoordinate, BoardCoordinate>>()
        val currentPieces = board.toList()
        for ((coord, piece) in currentPieces) {
            if (piece.color == currentTurn) {
                val targets = getLegalMovesForPiece(coord)
                for (target in targets) {
                    allLegalMoves.add(coord to target)
                }
            }
        }

        val inCheck = isKingInCheck(currentTurn)

        if (allLegalMoves.isEmpty()) {
            if (inCheck) {
                gameResult = if (currentTurn == PieceColor.WHITE) GameResult.BLACK_VICTORY else GameResult.WHITE_VICTORY
            } else {
                gameResult = GameResult.STALEMATE
            }
        } else {
            gameResult = GameResult.ONGOING
        }
    }

    // AI calculation
    fun computeBestAiMove(difficulty: Int): Pair<BoardCoordinate, BoardCoordinate>? {
        val moves = mutableListOf<Pair<BoardCoordinate, BoardCoordinate>>()
        val currentPieces = board.toList()
        for ((coord, piece) in currentPieces) {
            if (piece.color == currentTurn) {
                val legalTargets = getLegalMovesForPiece(coord)
                for (t in legalTargets) {
                    moves.add(coord to t)
                }
            }
        }
        if (moves.isEmpty()) return null

        // Easy / Cadet AI: evaluates captures + random
        if (difficulty <= 1) {
            moves.shuffle()
            moves.sortByDescending { (from, to) ->
                val target = board[to]
                val attacker = board[from]
                val captureValue = target?.type?.value ?: 0
                val hazardBonus = if (hazards[to] == CosmicHazard.GRAVITY_WELL) 40 else 0
                captureValue * 10 - (attacker?.type?.value ?: 0) / 10 + hazardBonus
            }
            return moves.first()
        }

        // Sentinel / Grandmaster AI: Minimax Depth 2 or 3
        var bestScore = Int.MIN_VALUE
        var bestMove = moves.first()

        val depth = if (difficulty >= 3) 2 else 2 // High performance & responsive

        for ((from, to) in moves) {
            val movedPiece = board[from]!!
            val capturedPiece = board[to]

            board.remove(from)
            board[to] = movedPiece

            val score = -minimax(depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, currentTurn.opposite())

            board[from] = movedPiece
            if (capturedPiece != null) board[to] = capturedPiece else board.remove(to)

            if (score > bestScore) {
                bestScore = score
                bestMove = from to to
            }
        }

        return bestMove
    }

    private fun minimax(depth: Int, alpha: Int, beta: Int, turn: PieceColor): Int {
        if (depth == 0) {
            return evaluateBoard(turn)
        }

        var curAlpha = alpha
        val moves = mutableListOf<Pair<BoardCoordinate, BoardCoordinate>>()
        val currentPieces = board.toList()
        for ((coord, piece) in currentPieces) {
            if (piece.color == turn) {
                for (t in getCandidateMoves(coord, piece)) {
                    if (isMoveLegal(coord, t, piece)) {
                        moves.add(coord to t)
                    }
                }
            }
        }

        if (moves.isEmpty()) {
            return if (isKingInCheck(turn)) -100000 else 0
        }

        for ((from, to) in moves) {
            val moved = board[from]!!
            val captured = board[to]
            board.remove(from)
            board[to] = moved

            val score = -minimax(depth - 1, -beta, -curAlpha, turn.opposite())

            board[from] = moved
            if (captured != null) board[to] = captured else board.remove(to)

            curAlpha = max(curAlpha, score)
            if (curAlpha >= beta) break
        }

        return curAlpha
    }

    private fun evaluateBoard(colorPerspective: PieceColor): Int {
        var whiteScore = 0
        var blackScore = 0

        for ((coord, piece) in board) {
            var pieceVal = piece.type.value
            // Center control bonus
            if (coord.row in 3..4 && coord.col in 3..4) {
                pieceVal += 25
            }
            if (piece.color == PieceColor.WHITE) {
                whiteScore += pieceVal
            } else {
                blackScore += pieceVal
            }
        }

        val net = if (colorPerspective == PieceColor.WHITE) (whiteScore - blackScore) else (blackScore - whiteScore)
        return net
    }
}

fun PieceColor.opposite(): PieceColor = if (this == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
