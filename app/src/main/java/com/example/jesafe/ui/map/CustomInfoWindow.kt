package com.example.jesafe.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jesafe.R
import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.model.Marker

@Composable
fun CustomInfoWindow(
    accident: AccidentEntity,
    marker: Marker
) {
    Column(
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .width(280.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ambulance),
                    contentDescription = "구급차",
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

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        InfoRow(label = "월", value = formatMonth(accident.reportDate) + " (${accident.season ?: "알수없음"})")
        InfoRow(label = "유형", value = accident.accidentType ?: "-")
        InfoRow(label = "증상", value = formatBooleanOrValue(accident.ptnSymSeNm, "-"))
        InfoRow(label = "중증외상", value = formatBooleanOrValue(accident.srilOncrNm))
        InfoRow(label = "교통사고", value = formatBooleanOrValue(accident.trfcAcdntSeNm))
        InfoRow(label = "위치", value = "(${String.format("%.4f", accident.latitude)}, ${String.format("%.4f", accident.longitude)})")
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
