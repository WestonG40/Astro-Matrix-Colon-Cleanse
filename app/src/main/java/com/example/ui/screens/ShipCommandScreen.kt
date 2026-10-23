package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.AnomalyLog
import com.example.model.ShipComponent
import com.example.model.ShipInventory
import com.example.ui.theme.CosmicVoid
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
fun ShipCommandScreen(
    viewModel: AstroMatrixViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val components by viewModel.shipComponents.collectAsState()
    val anomalies by viewModel.anomalyLogs.collectAsState()
    val sectors by viewModel.sectors.collectAsState()

    val liberatedCount = sectors.values.count { it.isLiberated }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Graphic & Starship Specs
        item {
            ShipHeroCard(liberatedCount = liberatedCount, totalSectors = sectors.size)
        }

        // Cargo Inventory Manifest
        item {
            CargoManifestCard(inventory = inventory)
        }

        // Engineering Upgrade Bay
        item {
            Text(
                text = "ENGINEERING UPGRADE BAY",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        itemsIndexed(components) { index, comp ->
            ComponentUpgradeCard(
                component = comp,
                onUpgrade = { viewModel.upgradeComponent(index) }
            )
        }

        // Scientific Codex / Anomaly Discoveries
        item {
            Text(
                text = "SCIENTIFIC CODEX & ANOMALIES (${anomalies.size})",
                color = StellarGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (anomalies.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCard),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No planetary anomalies cataloged yet.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Fly close to orbiting planets in Orbit Flight mode and tap 'Survey Near Body' to discover ancient matrix ruins and unlock rare resources.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(anomalies) { _, log ->
                AnomalyLogCard(log = log)
            }
        }
    }
}

@Composable
fun ShipHeroCard(liberatedCount: Int, totalSectors: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ship_hero_card"),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.img_galaxy_hero)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Starship Cockpit & Galaxy Matrix",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CosmicVoid.copy(alpha = 0.85f))
                            )
                        )
                )

                // Vessel Name Badge
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "USSC MATRIX PIONEER",
                        color = StarWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Quantum Exploration & Flagship Dreadnought",
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }

            // Stats / Progress Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GALAXY SECTORS LIBERATED",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$liberatedCount / $totalSectors",
                            color = OrbitGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Secured",
                            tint = OrbitGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TACTICAL ENGINE RATING",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MK IV SINGULARITY",
                        color = StellarGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun CargoManifestCard(inventory: ShipInventory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cargo_manifest_card"),
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CARGO MANIFEST & RESOURCES",
                color = StarWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CargoItem(label = "Plasma Fuel", value = "${inventory.plasmaFuel}/${inventory.maxFuel}", icon = "⚡", color = NeonCyan)
                CargoItem(label = "Quantum Shards", value = "${inventory.quantumCrystals}", icon = "💎", color = QuantumViolet)
                CargoItem(label = "Dark Energy", value = "${inventory.darkEnergy}", icon = "🔮", color = HyperMagenta)
                CargoItem(label = "Exotic Alloys", value = "${inventory.exoticAlloys}", icon = "🛡️", color = StellarGold)
                CargoItem(label = "Stellar Data", value = "${inventory.stellarDataPoints}", icon = "📜", color = OrbitGreen)
            }
        }
    }
}

@Composable
fun CargoItem(label: String, value: String, icon: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label.take(6),
            color = Color.LightGray,
            fontSize = 9.sp
        )
    }
}

@Composable
fun ComponentUpgradeCard(
    component: ShipComponent,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upgrade_${component.name.take(6)}"),
        colors = CardDefaults.cardColors(containerColor = SpaceCardElevated),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = component.name,
                        color = StarWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mk ${component.level}/${component.maxLevel}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = component.description,
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                Text(
                    text = "Bonus: ${component.bonusStat}",
                    color = OrbitGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (component.level < component.maxLevel) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(
                        text = "Upgrade (${component.upgradeCostFuel}F / ${component.upgradeCostCrystals}C)",
                        color = CosmicVoid,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "MAX MK",
                    color = StellarGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AnomalyLogCard(log: AnomalyLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.title,
                    color = StellarGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sector ${log.sectorNotation}",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = log.description,
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
