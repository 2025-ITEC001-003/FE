package com.example.jesafe.ui.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * 사고 유형별로 마커 색상을 다르게 표시하는 클러스터 렌더러
 */
class AccidentClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AccidentClusterItem>
) : DefaultClusterRenderer<AccidentClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(
        item: AccidentClusterItem,
        markerOptions: MarkerOptions
    ) {
        val accident = item.getAccident()
        val markerColor = getMarkerColorForAccidentType(accident.accidentType)

        markerOptions
            .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            .title(item.title)
            .snippet(item.snippet)

        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    private fun getMarkerColorForAccidentType(accidentType: String?): Float {
        return when (accidentType?.uppercase()) {
            "차대사람" -> BitmapDescriptorFactory.HUE_RED        // 빨강 - 가장 위험
            "차대차" -> BitmapDescriptorFactory.HUE_ORANGE      // 주황
            "차량단독" -> BitmapDescriptorFactory.HUE_YELLOW    // 노랑
            "철길건널목" -> BitmapDescriptorFactory.HUE_MAGENTA  // 자주
            else -> BitmapDescriptorFactory.HUE_AZURE           // 하늘색 - 기타
        }
    }
}

