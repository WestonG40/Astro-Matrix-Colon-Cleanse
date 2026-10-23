package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardCoordinate
import com.example.model.ChessPiece
import com.example.model.CosmicHazard
import com.example.model.GameResult
import com.example.model.PieceColor
import com.example.model.PieceType
import com.example.model.QuantumGambit
import com.example.ui.theme.BoardCaptureTarget
import com.example.ui.theme.BoardGravityWell
import com.example.ui.theme.BoardHighlight
import com.example.ui.theme.BoardMoveTarget
import com.example.ui.theme.BoardTileDark
import com.example.ui.theme.BoardTileLight
import com.example.ui.theme.BoardWormhole
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HyperMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OrbitGreen
import com.example.ui.theme.QuantumViolet
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StellarGold
import com.example.viewmodel.AstroMatrixViewModel

@Composable
fun MatrixChessScreen(
    viewModel: AstroMatrixViewModel,
    modifier: Modifier = Modifier
) {
    // Read state
    val boardVersion by viewModel.chessBoardVersion.collectAsState()
    val selectedSquare by viewModel.selectedChessSquare.collectAsState()
    val validMoves by viewModel.validChessMoves.collectAsState()
    val activeGambit by viewModel.activeGambit.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aiDifficulty by viewModel.aiDifficulty.collectAsState()
    val isVsAi by viewModel.isVsAi.collectAsState()

    val engine = viewModel.chessEngine
    val lastMove = engine.moveHistory.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Player Duel Header (Black / Sentinel vs White / Explorer)
        DuelHeader(
            engine = engine,
            isAiThinking = isAiThinking,
            isVsAi = isVsAi,
            aiDifficulty = aiDifficulty,
            onToggleVsAi = { viewModel.setVsAi(!isVsAi) },
            onCycleDifficulty = {
                val next = when (aiDifficulty) {
                    1 -> 2
                    2 -> 3
                    else -> 1
                }
                viewModel.setAiDifficulty(next)
            },
            onResetBoard = { viewModel.resetChessBoard() }
        )

        // Game Result Banner if Checkmate / Stalemate
        if (engine.gameResult != GameResult.ONGOING) {
            GameResultBanner(
                result = engine.gameResult,
                onRematch = { viewModel.resetChessBoard() }
            )
        }

        // 8x8 Chessboard Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(SpaceCard, RoundedCornerShape(8.dp))
                .border(1.5.dp, if (engine.currentTurn == PieceColor.WHITE) NeonCyan.copy(alpha = 0.5f) else HyperMagenta.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(4.dp)
                .testTag("matrix_chessboard")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0..7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0..7) {
                            val coord = BoardCoordinate(r, c)
                            val piece = engine.getPiece(coord)
                            val hazard = engine.getHazard(coord)
                            val isSelected = coord == selectedSquare
                            val isValidMove = coord in validMoves
                            val isLastMove = lastMove?.from == coord || lastMove?.to == coord

                            ChessSquareCell(
                                coord = coord,
                                piece = piece,
                                hazard = hazard,
                                isSelected = isSelected,
                                isValidMove = isValidMove,
                                isLastMove = isLastMove,
                                onClick = { viewModel.onSquareClicked(coord) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Quantum Tactical Gambits Bar
        QuantumGambitBar(
            currentEnergy = if (engine.currentTurn == PieceColor.WHITE) engine.whiteEnergy else engine.blackEnergy,
            activeGambit = activeGambit,
            onSelectGambit = { viewModel.selectGambit(it) },
            modifier = Modifier.fillMaxWidth()
        )

        // Captured Pieces & Move History Ticker
        MoveHistoryTicker(engine = engine)
    }
}

@Composable
fun DuelHeader(
    engine: com.example.engine.MatrixChessEngine,
    isAiThinking: Boolean,
    isVsAi: Boolean,
    aiDifficulty: Int,
    onToggleVsAi: () -> Unit,
    onCycleDifficulty: () -> Unit,
    onResetBoard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Row: Commander titles & Opponent mode toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // White Fleet info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (engine.currentTurn == PieceColor.WHITE) NeonCyan else Color.DarkGray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXPLORER (WHITE)",
                        color = if (engine.currentTurn == PieceColor.WHITE) NeonCyan else StarWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // AI Difficulty / Mode Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onToggleVsAi,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = SpaceCard)
                    ) {
                        Text(
                            text = if (isVsAi) "VS AI" else "2-PLAYER",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isVsAi) {
                        FilledTonalButton(
                            onClick = onCycleDifficulty,
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SpaceCard)
                        ) {
                            Text(
                                text = when (aiDifficulty) {
                                    1 -> "Cadet"
                                    2 -> "Sentinel"
                                    else -> "Archon"
                                },
                                color = StellarGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onResetBoard,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Board",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Black Sentinel info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAiThinking) "THINKING..." else "SENTINEL (BLACK)",
                        color = if (engine.currentTurn == PieceColor.BLACK) HyperMagenta else StarWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (engine.currentTurn == PieceColor.BLACK) HyperMagenta else Color.DarkGray, CircleShape)
                    )
                }
            }

            // Energy Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // White Energy
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QE: ${engine.whiteEnergy}",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { engine.whiteEnergy / 100f },
                        color = NeonCyan,
                        trackColor = SpaceCard,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Black Energy
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    LinearProgressIndicator(
                        progress = { engine.blackEnergy / 100f },
                        color = HyperMagenta,
                        trackColor = SpaceCard,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(4.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "QE: ${engine.blackEnergy}",
                        color = HyperMagenta,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ChessSquareCell(
    coord: BoardCoordinate,
    piece: ChessPiece?,
    hazard: CosmicHazard,
    isSelected: Boolean,
    isValidMove: Boolean,
    isLastMove: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightSquare = (coord.row + coord.col) % 2 == 0
    val baseSquareColor = if (isLightSquare) BoardTileLight else BoardTileDark

    val backgroundBrush = when {
        isSelected -> BoardHighlight
        isValidMove && piece != null -> BoardCaptureTarget
        isValidMove -> BoardMoveTarget
        hazard == CosmicHazard.GRAVITY_WELL -> BoardGravityWell
        hazard == CosmicHazard.WORMHOLE_ALPHA || hazard == CosmicHazard.WORMHOLE_BETA -> BoardWormhole
        isLastMove -> Color(0x3300F0FF)
        else -> baseSquareColor
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(backgroundBrush, RoundedCornerShape(3.dp))
            .border(
                if (isSelected) 1.5.dp else if (isLastMove) 1.dp else 0.5.dp,
                if (isSelected) NeonCyan else if (isLastMove) NeonCyan.copy(alpha = 0.5f) else SpaceBorder.copy(alpha = 0.3f),
                RoundedCornerShape(3.dp)
            )
            .clickable { onClick() }
            .testTag("square_${coord.notation}"),
        contentAlignment = Alignment.Center
    ) {
        // Background hazard icon if empty
        if (hazard != CosmicHazard.NONE && piece == null) {
            Text(
                text = hazard.icon,
                fontSize = 11.sp
            )
        }

        // Chess Piece
        if (piece != null) {
            val pieceColor = if (piece.color == PieceColor.WHITE) NeonCyan else StarWhite
            val pieceStroke = if (piece.color == PieceColor.WHITE) Color.Transparent else HyperMagenta

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = piece.type.symbol,
                    color = pieceColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shield visual indicator
            if (piece.shieldTurnsRemaining > 0) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(QuantumViolet, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }

        // Valid move dot if empty
        if (isValidMove && piece == null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(OrbitGreen, CircleShape)
            )
        }

        // Notation label in corner (rank & file)
        if (coord.col == 0) {
            Text(
                text = "${8 - coord.row}",
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 7.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(1.dp)
            )
        }
        if (coord.row == 7) {
            val fileChar = ('a'.code + coord.col).toChar().toString()
            Text(
                text = fileChar,
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 7.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(1.dp)
            )
        }
    }
}

@Composable
fun QuantumGambitBar(
    currentEnergy: Int,
    activeGambit: QuantumGambit?,
    onSelectGambit: (QuantumGambit) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("gambit_bar"),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "QUANTUM MATRIX GAMBITS",
                color = StellarGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (gambit in QuantumGambit.entries) {
                    val isActive = activeGambit == gambit
                    val canAfford = currentEnergy >= gambit.cost

                    FilledTonalButton(
                        onClick = { onSelectGambit(gambit) },
                        enabled = canAfford,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("gambit_${gambit.name}"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isActive) StellarGold else SpaceCard,
                            contentColor = if (isActive) CosmicVoid else StarWhite
                        )
                    ) {
                        Text(
                            text = "${gambit.icon} ${gambit.title.take(8)} (${gambit.cost})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoveHistoryTicker(engine: com.example.engine.MatrixChessEngine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceCard, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "LOG: ",
            color = Color.LightGray,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val moves = engine.moveHistory.takeLast(6)
            items(moves) { move ->
                Text(
                    text = move.notation,
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Captured White & Black tallies
        Text(
            text = "Caps: +${engine.capturedByWhite.size} / -${engine.capturedByBlack.size}",
            color = OrbitGreen,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GameResultBanner(
    result: GameResult,
    onRematch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (result == GameResult.WHITE_VICTORY) OrbitGreen.copy(alpha = 0.25f) else DangerRed.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (result == GameResult.WHITE_VICTORY) OrbitGreen else DangerRed,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .testTag("game_result_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result.message,
                color = StarWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onRematch,
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(
                    text = "Rematch",
                    color = CosmicVoid,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
