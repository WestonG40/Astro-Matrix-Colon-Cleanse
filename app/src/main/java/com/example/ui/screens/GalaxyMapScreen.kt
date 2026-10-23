package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import com.example.model.Planet
import com.example.model.SectorCoordinate
import com.example.model.StarSpectralClass
import com.example.model.StarSystem
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HyperMagenta
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OrbitGreen
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StellarGold
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.AstroMatrixViewModel

@Composable
fun GalaxyMapScreen(
    viewModel: AstroMatrixViewModel,
    modifier: Modifier = Modifier
) {
    val sectors by viewModel.sectors.collectAsState()
    val currentSectorId by viewModel.currentSectorId.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Starchart Title & Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PROCEDURAL MATRIX ATLAS",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "8x8 Interstellar Chessboard Quadrant",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            // Legend indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(color = OrbitGreen, label = "Liberated")
                LegendItem(color = DangerRed, label = "Hostile")
                LegendItem(color = NeonCyan, label = "Current")
            }
        }

        // 8x8 Sector Matrix Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(SpaceCard, RoundedCornerShape(8.dp))
                .border(1.dp, SpaceBorder, RoundedCornerShape(8.dp))
                .padding(4.dp)
                .testTag("galaxy_grid")
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
                            val coordId = "${r}_${c}"
                            val system = sectors[coordId]
                            val isCurrent = coordId == currentSectorId
                            val isSelected = system?.id == selectedSector?.id

                            if (system != null) {
                                SectorCell(
                                    system = system,
                                    isCurrent = isCurrent,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectSector(system) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Selected Sector Details Panel
        val targetSystem = selectedSector ?: sectors[currentSectorId]
        if (targetSystem != null) {
            SectorDetailCard(
                system = targetSystem,
                isCurrent = targetSystem.id == currentSectorId,
                onJump = { viewModel.jumpToSector(targetSystem) },
                onChallengeSentinel = {
                    viewModel.setAiDifficulty(targetSystem.sentinelThreatLevel.coerceIn(1, 3))
                    viewModel.setScreen(ActiveScreen.MATRIX_CHESS)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
fun SectorCell(
    system: StarSystem,
    isCurrent: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val starColor = Color(system.spectralClass.colorHex)
    val borderColor = when {
        isCurrent -> NeonCyan
        isSelected -> StellarGold
        system.isLiberated -> OrbitGreen.copy(alpha = 0.4f)
        else -> SpaceBorder.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(
                if (isSelected) SpaceCardElevated else CosmicVoid,
                RoundedCornerShape(3.dp)
            )
            .border(if (isCurrent || isSelected) 1.5.dp else 0.5.dp, borderColor, RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .testTag("sector_cell_${system.coordinate.notation}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Star circle indicator
            Box(
                modifier = Modifier
                    .size(if (system.spectralClass == StarSpectralClass.BLACK_HOLE) 9.dp else 7.dp)
                    .background(starColor, CircleShape)
            )

            // Notation label (e.g. E4)
            Text(
                text = system.coordinate.notation,
                color = if (isCurrent) NeonCyan else Color.White.copy(alpha = 0.8f),
                fontSize = 7.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        // Status pip
        if (system.isLiberated) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
                    .size(4.dp)
                    .background(OrbitGreen, CircleShape)
            )
        } else if (system.sentinelThreatLevel >= 4) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
                    .size(4.dp)
                    .background(DangerRed, CircleShape)
            )
        }
    }
}

@Composable
fun SectorDetailCard(
    system: StarSystem,
    isCurrent: Boolean,
    onJump: () -> Unit,
    onChallengeSentinel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("sector_detail_card"),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        LazyColumn(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = system.name,
                            color = StarWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${system.spectralClass.title} • Coord: ${system.coordinate.notation}",
                            color = Color(system.spectralClass.colorHex),
                            fontSize = 11.sp
                        )
                    }

                    // Threat level badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (system.isLiberated) OrbitGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (system.isLiberated) OrbitGreen else DangerRed,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (system.isLiberated) "SECURED" else "THREAT LV ${system.sentinelThreatLevel}",
                            color = if (system.isLiberated) OrbitGreen else DangerRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    text = system.loreSnippet,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            // Planets Overview
            item {
                Text(
                    text = "PLANETARY BODIES (${system.planets.size})",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(system.planets) { planet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceCard, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(planet.colorHex), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = planet.name,
                            color = StarWhite,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${planet.yield} ${planet.resource.label}",
                        color = Color(planet.resource.colorHex),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isCurrent) {
                        Button(
                            onClick = onJump,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("jump_sector_btn"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Warp Jump",
                                tint = CosmicVoid,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Warp Jump (-20 Fuel)",
                                color = CosmicVoid,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onChallengeSentinel,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("challenge_sentinel_btn"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = HyperMagenta)
                    ) {
                        Text(
                            text = if (system.isLiberated) "Practice Duel" else "Challenge Sentinel",
                            color = StarWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.Gray, fontSize = 9.sp)
    }
}
