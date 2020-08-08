package com.openld.mapsample.interfaces

import com.openld.mapsample.bean.POIBean

interface OnPoiItemClickListener {
    fun onClickPoiItem(position: Int, poiBean: POIBean)
}
