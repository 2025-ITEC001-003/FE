package com.example.jesafe.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jesafe.data.local.AccidentEntity

@Composable
fun CustomInfoWindow(
    accident: AccidentEntity,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "사고 위치",
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "사고 정보 (ID: ${accident.id})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007AFF)
                )
            }
            
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        InfoRow(label = "월", value = formatMonth(accident.reportDate) + " (${accident.season ?: "알수없음"})")
        InfoRow(label = "유형", value = accident.accidentType ?: "-")
        InfoRow(label = "증상", value = formatBooleanOrValue(accident.ptnSymSeNm, "-"))
        InfoRow(label = "중증외상", value = formatBooleanOrValue(accident.srilOncrNm))
        InfoRow(label = "교통사고", value = formatBooleanOrValue(accident.trfcAcdntSeNm))
        InfoRow(label = "위치", value = "(${String.format("%.4f", accident.latitude)}, ${String.format("%.4f", accident.longitude)})")
    }
}

@Composable
fun ClusterInfoWindow(
    cluster: ClusterArea,
    onClose: () -> Unit
) {
    val clusterColor = ClusterUtils.getClusterColor(cluster.clusterId)

    Column(
        modifier = Modifier
            .width(320.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Cluster",
                    tint = clusterColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "클러스터 ${cluster.clusterId}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = clusterColor
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basic Info
        val redPinColor = Color(0xFFE91E63)
        InfoItem(Icons.Default.PushPin, "사고 건수:", "${cluster.count}건", redPinColor)
        InfoItem(Icons.Default.PushPin, "중심 좌표:", "(${String.format("%.4f", cluster.center.latitude)}, ${String.format("%.4f", cluster.center.longitude)})", redPinColor)
        InfoItem(Icons.Default.Straighten, "반지름:", "${cluster.radius.toInt()}m (최대 거리: ${cluster.maxDistance.toInt()}m)", Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        // Disease Stats
        SectionHeader(Icons.Default.LocalHospital, "질병 분류", Color.Gray) 
        cluster.stats.diseaseStats.forEach { (key, value) ->
            val iconTint = if (key == "질병") Color(0xFFFF5252) else Color(0xFF448AFF)
            val icon = Icons.Default.Medication // Pill icon
            StatRow(label = key, percent = value, color = iconTint, icon = icon)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Severity Stats
        SectionHeader(Icons.Default.Assessment, "사고 심각도", Color.Gray)
        cluster.stats.severityStats.forEach { (key, value) ->
            val (icon, tint) = if (key == "교통사고") {
                 Pair(Icons.Default.DirectionsCar, Color(0xFFFF5252))
            } else {
                 Pair(Icons.Default.MedicalServices, Color(0xFFAA00FF)) // Purple
            }
            StatRow(label = key, percent = value, color = tint, icon = icon)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(Icons.Default.DateRange, "시간 분포 (Top 3)", Color.Gray)
        
        if (cluster.stats.topMonths.isNotEmpty()) {
            Text("주요 월:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
            cluster.stats.topMonths.take(3).forEach { (month, percent) ->
                StatRow(label = month, percent = percent, color = Color.DarkGray, icon = null)
            }
        }

        if (cluster.stats.topSeasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("주요 계절:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
            cluster.stats.topSeasons.take(3).forEach { (season, percent) ->
                StatRow(label = season, percent = percent, color = Color.DarkGray, icon = null)
            }
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, label: String, value: String, iconTint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatRow(label: String, percent: Float, color: Color, icon: ImageVector?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                 Spacer(modifier = Modifier.width(8.dp))
            } else {
                // Align text when no icon
            }
           
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
        Text(
            text = String.format("%.1f%%", percent),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

fun formatMonth(dateStr: String?): String {
    if (dateStr.isNullOrLength(8)) return "알수없음"
    val month = dateStr!!.substring(4, 6).toIntOrNull()
    return if (month != null) "${month}월" else "알수없음"
}

fun formatBooleanOrValue(value: String?, defaultValue: String = "아니오"): String {
    return when (value?.uppercase()) {
        null, "" -> defaultValue
        "Y", "YES" -> "예"
        "N", "NO" -> "아니오"
        else -> value
    }
}

private fun String?.isNullOrLength(len: Int): Boolean {
    return this == null || this.length != len
}
