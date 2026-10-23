package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShipInventory
import com.example.model.StarSystem
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.OrbitGreen
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceCardElevated
import com.example.ui.theme.StarWhite
import com.example.ui.theme.StellarGold

@Composable
fun CosmicTopBar(
    system: StarSystem?,
    inventory: ShipInventory,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CosmicVoid.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Top info line: Sector notation + Star name + Resources
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sector & System info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, NeonCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("sector_badge")
                ) {
                    Text(
                        text = system?.coordinate?.notation ?: "??",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = system?.name ?: "Unknown Sector",
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = system?.spectralClass?.title ?: "",
                        color = Color(system?.spectralClass?.colorHex ?: 0xFFFFFFFF),
                        fontSize = 10.sp
                    )
                }
            }

            // Resource Pill Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ResourceBadge(label = "Fuel", value = "${inventory.plasmaFuel}", color = NeonCyan)
                ResourceBadge(label = "Shards", value = "${inventory.quantumCrystals}", color = Color(0xFFB388FF))
                ResourceBadge(label = "DarkE", value = "${inventory.darkEnergy}", color = Color(0xFFFF4081))
            }
        }

        // Live notification banner
        AnimatedVisibility(
            visible = statusMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            if (statusMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(SpaceCardElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, SpaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("status_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Status",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusMessage,
                            color = StarWhite,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceBadge(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .background(SpaceCard, RoundedCornerShape(4.dp))
            .border(0.8.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                color = Color.LightGray,
                fontSize = 10.sp
            )
            Text(
                text = value,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
