package com.example.jesafe.ui.map

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.abs

object ClusterUtils {

    /**
     * 클러스터 ID에 따라 고유한 색상을 반환합니다.
     * 노이즈(-1)인 경우 회색을 반환합니다.
     */
    fun getClusterColor(clusterId: Int): Color {
        if (clusterId == -1) return Color.Gray
        
        // Golden Angle (137.5도)을 사용하여 색상을 고르게 분포
        val hue = (clusterId * 137.508f) % 360
        return Color.hsv(hue, 0.7f, 0.9f)
    }

    /**
     * Google Maps Marker용 색상 (HUE 값) 반환
     */
    fun getMarkerHue(clusterId: Int): Float {
        if (clusterId == -1) return BitmapDescriptorFactory.HUE_VIOLET // 노이즈는 별도 색상 처리 (또는 회색 마커 아이콘 사용)
        
        val hue = (clusterId * 137.508f) % 360
        return hue
    }
    
    /**
     * Compose Color를 16진수 문자열로 변환 (#RRGGBB)
     */
    fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
