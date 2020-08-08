package com.openld.mapsample.bean

import com.amap.api.services.core.PoiItem
import java.io.Serializable

/**
 * POI对象
 */
data class POIBean(val province: String,
                   val provinceCode: String,
                   val city: String,
                   val cityCode: String,
                   val area: String,
                   val areaCode: String,
                   val latitude: Double = 0.0,
                   val longitude: Double = 0.0,
                   val poiName: String,
                   val address: String) : Serializable {
    override fun toString(): String {
        return "POIBean(province='$province', provinceCode='$provinceCode', city='$city', cityCode='$cityCode', area='$area', areaCode='$areaCode', latitude=$latitude, longitude=$longitude, poiName='$poiName', address='$address')"
    }
}
