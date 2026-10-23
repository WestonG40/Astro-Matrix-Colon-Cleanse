package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Planet
import com.example.model.ShipFlightTelemetry
import com.example.model.StarSystem
import com.example.model.Vector2D
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
import com.example.viewmodel.AutopilotMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrbitSimulatorScreen(
    viewModel: AstroMatrixViewModel,
    modifier: Modifier = Modifier
) {
    val currentSectorId by viewModel.currentSectorId.collectAsState()
    val sectors by viewModel.sectors.collectAsState()
    val starSystem = sectors[currentSectorId]
    val telemetry by viewModel.telemetry.collectAsState()
    val autopilot by viewModel.autopilot.collectAsState()
    val timeWarp by viewModel.timeWarp.collectAsState()

    var zoomScale by remember { mutableFloatStateOf(0.75f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
    ) {
        val screenW = maxWidth.value
        val screenH = maxHeight.value

        // 2D Physics Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(
                            (panOffset.x + dragAmount.x).coerceIn(-300f, 300f),
                            (panOffset.y + dragAmount.y).coerceIn(-300f, 300f)
                        )
                    }
                }
                .testTag("orbit_canvas")
        ) {
            val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)

            if (starSystem != null) {
                // Draw background grid lines (procedural spatial matrix)
                val gridStep = 60f * zoomScale
                val numGrid = 15
                for (i in -numGrid..numGrid) {
                    val lineX = center.x + i * gridStep
                    drawLine(
                        color = Color(0x1500F0FF),
                        start = Offset(lineX, 0f),
                        end = Offset(lineX, size.height),
                        strokeWidth = 1f
                    )
                    val lineY = center.y + i * gridStep
                    drawLine(
                        color = Color(0x1500F0FF),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1f
                    )
                }

                // Draw central star corona and core
                val starColor = Color(starSystem.spectralClass.colorHex)
                val starVisualRadius = starSystem.radius * zoomScale

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            starColor.copy(alpha = 0.85f),
                            starColor.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = starVisualRadius * 2.8f
                    ),
                    center = center,
                    radius = starVisualRadius * 2.8f
                )
                drawCircle(
                    color = starColor,
                    center = center,
                    radius = starVisualRadius
                )

                // Asteroid Belt if present
                if (starSystem.asteroidBeltRadius != null) {
                    drawCircle(
                        color = Color(0x33B388FF),
                        center = center,
                        radius = starSystem.asteroidBeltRadius * zoomScale,
                        style = Stroke(
                            width = 6f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                        )
                    )
                }

                // Draw Planets and Orbit Lines
                for (planet in starSystem.planets) {
                    val orbitR = planet.orbitalRadius * zoomScale
                    // Orbital track
                    drawCircle(
                        color = Color(0x288B9CB5),
                        center = center,
                        radius = orbitR,
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 14f), 0f)
                        )
                    )

                    // Planet Position
                    val pPos = planet.getPosition(Vector2D(0f, 0f))
                    val pScreenPos = Offset(
                        center.x + pPos.x * zoomScale,
                        center.y + pPos.y * zoomScale
                    )
                    val pColor = Color(planet.colorHex)
                    val pRadius = planet.radius * zoomScale

                    // Atmosphere halo
                    drawCircle(
                        color = pColor.copy(alpha = 0.35f),
                        center = pScreenPos,
                        radius = pRadius * 1.5f
                    )

                    // Rings if present
                    if (planet.hasRings) {
                        drawOval(
                            color = pColor.copy(alpha = 0.5f),
                            topLeft = Offset(pScreenPos.x - pRadius * 2.2f, pScreenPos.y - pRadius * 0.8f),
                            size = androidx.compose.ui.geometry.Size(pRadius * 4.4f, pRadius * 1.6f),
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // Planet core
                    drawCircle(
                        color = pColor,
                        center = pScreenPos,
                        radius = pRadius
                    )

                    // Survey marker if near
                    if (telemetry.position.distanceTo(pPos) <= planet.radius + 60f) {
                        drawCircle(
                            color = OrbitGreen,
                            center = pScreenPos,
                            radius = (pRadius + 14f) * zoomScale,
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
                }

                // Draw Predictive Trajectory (Keplerian orbital future path)
                if (telemetry.trajectoryPoints.isNotEmpty()) {
                    val path = Path()
                    var first = true
                    for (pt in telemetry.trajectoryPoints) {
                        val screenPt = Offset(center.x + pt.x * zoomScale, center.y + pt.y * zoomScale)
                        if (first) {
                            path.moveTo(screenPt.x, screenPt.y)
                            first = false
                        } else {
                            path.lineTo(screenPt.x, screenPt.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = if (telemetry.isOrbitStable) OrbitGreen.copy(alpha = 0.8f) else HyperMagenta.copy(alpha = 0.8f),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    )
                }

                // Draw Spacecraft
                val shipScreenPos = Offset(
                    center.x + telemetry.position.x * zoomScale,
                    center.y + telemetry.position.y * zoomScale
                )

                // Velocity vector arrow
                val velArrowEnd = Offset(
                    shipScreenPos.x + telemetry.velocity.x * 12f * zoomScale,
                    shipScreenPos.y + telemetry.velocity.y * 12f * zoomScale
                )
                drawLine(
                    color = NeonCyan,
                    start = shipScreenPos,
                    end = velArrowEnd,
                    strokeWidth = 2.5f
                )

                // Render Spacecraft triangle rotated to heading
                val headingDeg = (telemetry.headingAngle * 180f / PI.toFloat()) + 90f
                rotate(degrees = headingDeg, pivot = shipScreenPos) {
                    val shipPath = Path().apply {
                        moveTo(shipScreenPos.x, shipScreenPos.y - 12f * zoomScale)
                        lineTo(shipScreenPos.x - 7f * zoomScale, shipScreenPos.y + 10f * zoomScale)
                        lineTo(shipScreenPos.x + 7f * zoomScale, shipScreenPos.y + 10f * zoomScale)
                        close()
                    }
                    drawPath(shipPath, color = StarWhite)

                    // Thruster plume if engine active
                    if (telemetry.thrustPercent > 0.05f) {
                        val plumeLength = (15f * telemetry.thrustPercent + 5f) * zoomScale
                        val plumePath = Path().apply {
                            moveTo(shipScreenPos.x - 4f * zoomScale, shipScreenPos.y + 10f * zoomScale)
                            lineTo(shipScreenPos.x, shipScreenPos.y + 10f * zoomScale + plumeLength)
                            lineTo(shipScreenPos.x + 4f * zoomScale, shipScreenPos.y + 10f * zoomScale)
                            close()
                        }
                        drawPath(plumePath, color = NeonCyan)
                    }
                }
            }
        }

        // Overlay Telemetry HUD (Top-Right / Floating)
        TelemetryHudOverlay(
            telemetry = telemetry,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
        )

        // Zoom & Reset Camera Controls (Top-Left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                modifier = Modifier
                    .size(38.dp)
                    .background(SpaceCard.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, SpaceBorder, CircleShape)
            ) {
                Text("+", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.35f) },
                modifier = Modifier
                    .size(38.dp)
                    .background(SpaceCard.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, SpaceBorder, CircleShape)
            ) {
                Text("-", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 0.75f
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(SpaceCard.copy(alpha = 0.85f), CircleShape)
                    .border(1.dp, SpaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Center View",
                    tint = StarWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Controls Deck (Throttle, Steering, Autopilot, Survey)
        FlightControlDeck(
            viewModel = viewModel,
            telemetry = telemetry,
            autopilot = autopilot,
            timeWarp = timeWarp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun TelemetryHudOverlay(
    telemetry: ShipFlightTelemetry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("telemetry_hud"),
        colors = CardDefaults.cardColors(containerColor = SpaceCard.copy(alpha = 0.88f)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "FLIGHT TELEMETRY",
                color = NeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VEL: ${String.format("%.2f", telemetry.currentSpeed)} km/s",
                    color = StarWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ALT: ${telemetry.altitudeFromStar.toInt()} km",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STATUS: ${if (telemetry.isOrbitStable) "STABLE ORBIT" else "ESCAPE TRAJECTORY"}",
                    color = if (telemetry.isOrbitStable) OrbitGreen else HyperMagenta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (telemetry.isOrbitStable) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pe: ${telemetry.orbitPeriapsis.toInt()} km",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Ap: ${telemetry.orbitApoapsis.toInt()} km",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "e: ${String.format("%.2f", telemetry.orbitEccentricity)}",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "NEAR: ${telemetry.nearestBodyName} (${telemetry.nearestBodyDistance.toInt()} km)",
                color = StellarGold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun FlightControlDeck(
    viewModel: AstroMatrixViewModel,
    telemetry: ShipFlightTelemetry,
    autopilot: AutopilotMode,
    timeWarp: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("flight_controls"),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Autopilot chips & Time warp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Autopilot Mode Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AutopilotChip(
                        label = "Manual",
                        selected = autopilot == AutopilotMode.MANUAL,
                        onClick = { viewModel.setAutopilot(AutopilotMode.MANUAL) }
                    )
                    AutopilotChip(
                        label = "Prograde",
                        selected = autopilot == AutopilotMode.PROGRADE,
                        onClick = { viewModel.setAutopilot(AutopilotMode.PROGRADE) }
                    )
                    AutopilotChip(
                        label = "Retrograde",
                        selected = autopilot == AutopilotMode.RETROGRADE,
                        onClick = { viewModel.setAutopilot(AutopilotMode.RETROGRADE) }
                    )
                }

                // Time Warp Button
                FilledTonalButton(
                    onClick = {
                        val nextWarp = when (timeWarp) {
                            1f -> 2f
                            2f -> 5f
                            else -> 1f
                        }
                        viewModel.setTimeWarp(nextWarp)
                    },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SpaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Time Warp",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${timeWarp.toInt()}x",
                        color = StarWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls row: Steer Left, Throttle Slider, Steer Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Steer Left Button
                Button(
                    onClick = { viewModel.rotateShip(-0.25f) },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("steer_left_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                        contentDescription = "RCS Steer Left",
                        tint = NeonCyan
                    )
                }

                // Throttle Slider & Tank Gauge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "THRUST: ${(telemetry.thrustPercent * 100).toInt()}%",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "TANK: ${telemetry.fuel.toInt()}%",
                            color = if (telemetry.fuel < 20f) DangerRed else OrbitGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = telemetry.thrustPercent,
                        onValueChange = { viewModel.setThrottle(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SpaceCard
                        ),
                        modifier = Modifier.testTag("throttle_slider")
                    )
                }

                // Steer Right Button
                Button(
                    onClick = { viewModel.rotateShip(0.25f) },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("steer_right_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "RCS Steer Right",
                        tint = NeonCyan
                    )
                }
            }

            // Quick Actions: Survey Planet & Refuel Tank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.surveyNearestPlanet() },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("survey_planet_btn"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Survey",
                        tint = CosmicVoid,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Survey Near Body",
                        color = CosmicVoid,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.refuelFlightTank() },
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("refuel_tank_btn"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SpaceCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Refuel",
                        tint = StellarGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Refuel (-25)",
                        color = StarWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AutopilotChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) NeonCyan.copy(alpha = 0.2f) else SpaceCard,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (selected) NeonCyan else SpaceBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("ap_chip_$label")
    ) {
        Text(
            text = label,
            color = if (selected) NeonCyan else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
