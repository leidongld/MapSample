package com.openld.mapsample

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.*
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.openld.mapsample.adapter.POIAdapter
import com.openld.mapsample.bean.POIBean
import com.openld.mapsample.interfaces.OnPoiItemClickListener
import java.util.*

/**
 * author: lllddd
 * created on: 2020/8/6 21:10
 * description:
 */
public class AMapActivity : AppCompatActivity(),
        OnPoiItemClickListener,
        GeocodeSearch.OnGeocodeSearchListener,
        PoiSearch.OnPoiSearchListener,
        AMapLocationListener,
        AMap.OnMapLoadedListener,
        AMap.OnMapClickListener,
        AMap.InfoWindowAdapter,
        AMap.OnMapTouchListener, SearchView.OnQueryTextListener, AMap.OnCameraChangeListener, View.OnClickListener {
    private val TAG: String = "AMapActivity"
    private val REQUEST_CODE_FOE_PERMISSIONS: Int = 1001

    private val POI_SEARCH_PAGE_NUM = 1
    private val POI_SEARCH_PAGE_SIZE = 20

    // 布局元素 城市
    private lateinit var mTxtCity: TextView
    private lateinit var mSearchView: SearchView
    private lateinit var mMapView: MapView
    private lateinit var mRcvPoiContainer: RecyclerView
    private lateinit var mImgLocation: ImageView

    // 列表相关
    private lateinit var mPOIAdapter: POIAdapter

    // 地图相关
    private val CURRENT_PAGE: Int = 1
    private val POI_DISTANCE: Float = 1000F
    private val ZOON_RATIO: Float = 15F
    private val POI_TYPE: String =
            "010000" + "|" +       // 汽车服务相关
                    "020000" + "|" +       // 汽车销售
                    "030000" + "|" +       // 汽车维修
                    "050000" + "|" +       // 餐饮服务
                    "060000" + "|" +       // 购物服务
                    "070000" + "|" +       // 生活服务
                    "080000" + "|" +       // 体育休闲服务
                    "090000" + "|" +       // 医疗保健服务
                    "100000" + "|" +       // 住宿服务
                    "110000" + "|" +       // 风景名胜
                    "120000" + "|" +       // 商务住宅
                    "130000" + "|" +       // 政府机构及社会团体
                    "140000" + "|" +       // 科教文化服务
                    "150000" + "|" +       // 交通设施服务
                    "160000" + "|" +       // 金融保险服务
                    "170000" + "|" +       // 公司企业
                    "180000" + "|" +       // 道路附属设施
                    "190000" + "|" +       // 地名地址信息
                    "200000" + "|"         // 公共设施

    private lateinit var mPoiBean: POIBean
    private var mCurrentCity: String = "上海"
    private lateinit var mGeocodeSearch: GeocodeSearch
    private var mPoiBeanList: ArrayList<POIBean> = ArrayList()
    private lateinit var mAMap: AMap
    private lateinit var mMarker: Marker
    private lateinit var mLocationClient: AMapLocationClient
    private lateinit var mLocationOptions: AMapLocationClientOption
    private lateinit var mCurrentLatLon: LatLng
    private lateinit var mPoiSearchQuery: PoiSearch.Query
    private lateinit var mPoiSearch: PoiSearch
    private var mCurrentZoom: Float = ZOON_RATIO

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_gd_map)

        initWidgets(savedInstanceState)

        initMap()

        doLocation();
    }

    /**
     * 开始定位
     */
    private fun doLocation() {
        val hasFineLocationPermission: Boolean
        val hasCoarseLocationPermission: Boolean

        val fineLocationPermissionRes = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        hasFineLocationPermission = PackageManager.PERMISSION_GRANTED == fineLocationPermissionRes

        val coarseLocationPermissionRes = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        hasCoarseLocationPermission = PackageManager.PERMISSION_GRANTED == fineLocationPermissionRes

        if (!hasCoarseLocationPermission && !hasFineLocationPermission) {// 无定位权限
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_FOE_PERMISSIONS)
        } else {// 有定位权限
            prepareForLocation()
            startLocation()
        }
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        mLocationClient.startLocation()
    }

    /**
     * 定位准备
     */
    private fun prepareForLocation() {
        mLocationClient = AMapLocationClient(this)
        mLocationClient.setLocationListener(this)

        mLocationOptions = AMapLocationClientOption()
        mLocationOptions.setOnceLocation(true)
        mLocationOptions.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
        mLocationOptions.setNeedAddress(true)
        mLocationOptions.setMockEnable(false)

        mLocationClient.setLocationOption(mLocationOptions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_CODE_FOE_PERMISSIONS != requestCode) {
            return
        }
        if (PackageManager.PERMISSION_GRANTED != grantResults[0] && PackageManager.PERMISSION_GRANTED != grantResults[1]) {
            geoAddress("上海市", "上海市")
        } else {
            prepareForLocation()
            startLocation()
        }
    }


    /**
     * 初始化地图
     */
    private fun initMap() {
        mMapView.map

        mAMap.setOnCameraChangeListener(this)
        mAMap.setOnMapLoadedListener(this)
        mAMap.setInfoWindowAdapter(this)
        mAMap.setOnMapClickListener(this)
        mAMap.setOnMapTouchListener(this)

        mGeocodeSearch = GeocodeSearch(this)
        mGeocodeSearch.setOnGeocodeSearchListener(this)

        mLocationClient = AMapLocationClient(this)
        mLocationOptions = AMapLocationClientOption()
    }

    /**
     * 初始化控件
     *
     * @param savedInstanceState
     */
    private fun initWidgets(savedInstanceState: Bundle?) {
        mTxtCity = findViewById(R.id.txt_city)

        mSearchView = findViewById(R.id.search_view)
        mSearchView.setOnQueryTextListener(this)

        mImgLocation = findViewById(R.id.img_location)
        mImgLocation.setOnClickListener(this)

        mRcvPoiContainer = findViewById(R.id.rcv_poi_container)
        mPOIAdapter = POIAdapter(this, mPoiBeanList)
        mPOIAdapter.setPoiItemClickListener(this)
        mRcvPoiContainer.adapter = mPOIAdapter

        mMapView = findViewById(R.id.map_view)
        mMapView.onCreate(savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
        mLocationClient.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    /**
     * 移动相机
     */
    private fun moveMapCamera(latLon: LatLng) {
        val cameraPosition = CameraPosition(latLon, mCurrentZoom, 0F, 0F)
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        mAMap.moveCamera(cameraUpdate)
    }

    /**
     * 正地址编码
     *
     * @param keyword 关键词
     * @param city 城市
     */
    private fun geoAddress(keyword: String, city: String) {
        val query = GeocodeQuery(keyword, city)
        mGeocodeSearch.getFromLocationNameAsyn(query)
    }

    /**
     * 逆地址编码
     *
     * @param latLng 纬经度
     */
    private fun regeoAddress(latLng: LatLng) {
        val point = LatLonPoint(latLng.latitude, latLng.longitude)
        val query = RegeocodeQuery(point, POI_DISTANCE, GeocodeSearch.AMAP)
        mGeocodeSearch.getFromLocationAsyn(query)
    }

    /**
     * 兴趣点点击监听
     *
     * @param position 位置
     * @param poiBean POI对象
     */
    override fun onClickPoiItem(position: Int, poiBean: POIBean) {
        val intent = Intent()
        intent.putExtra("poiBean", poiBean)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * 逆地址编码回调
     *
     * @param p0 逆地址编码对象
     * @param p1 返回码
     */
    override fun onRegeocodeSearched(p0: RegeocodeResult?, p1: Int) {
        if (AMapException.CODE_AMAP_SUCCESS == p1 && p0 != null) {
            val regeocodeAddress: RegeocodeAddress = p0.regeocodeAddress
            mCurrentCity = regeocodeAddress.city
            val keyWord = regeocodeAddress.formatAddress
            doPoiSearch(keyWord, POI_TYPE, mCurrentCity)
            moveMapCamera(mCurrentLatLon)
        } else {
            Log.e(TAG, "onRegeocodeSearched error")
        }
    }

    /**
     * 正地址编码回调
     *
     * @param p0 正地址编码对象
     * @param p1 返回码
     */
    override fun onGeocodeSearched(p0: GeocodeResult?, p1: Int) {
        if (AMapException.CODE_AMAP_SUCCESS == p1 && p0 != null) {
            val geocodeAddressList = p0.geocodeAddressList
            if (geocodeAddressList == null || geocodeAddressList.isEmpty()) {
                return
            }
            val geocodeAddress: GeocodeAddress = geocodeAddressList[0]
            mCurrentCity = geocodeAddress.city
            mCurrentLatLon = LatLng(geocodeAddress.latLonPoint.latitude, geocodeAddress.latLonPoint.longitude)
            doPoiSearch(geocodeAddress.formatAddress, POI_TYPE, mCurrentCity)
            moveMapCamera(mCurrentLatLon)
        } else {
            Log.e(TAG, "onGeocodeSearched error")
        }
    }

    /**
     * 查找POI
     *
     * @param keyWord 關鍵詞
     * @param poiType POI類型限定
     * @param city 城市
     */
    private fun doPoiSearch(keyWord: String?, poiType: String, city: String?) {
        mPoiSearchQuery = PoiSearch.Query(keyWord, poiType, city)
        mPoiSearchQuery.pageSize = POI_SEARCH_PAGE_SIZE
        mPoiSearchQuery.pageNum = POI_SEARCH_PAGE_NUM

        val mPoiSearch = PoiSearch(this, mPoiSearchQuery)
        val latLonPoint = LatLonPoint(mCurrentLatLon.latitude, mCurrentLatLon.longitude)
        val searchBound = PoiSearch.SearchBound(latLonPoint, POI_DISTANCE.toInt())
        mPoiSearch.bound = searchBound
        mPoiSearch.searchPOIAsyn()
    }

    /**
     * 单条POI搜索回调
     *
     * @param p0 单挑POI搜索对象
     * @param p1 返回码
     */
    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {
        if (AMapException.CODE_AMAP_SUCCESS == p1 && p0 != null) {

        } else {
            Log.e(TAG, "onPoiItemSearched error")
        }
    }

    /**
     * POI搜索回调
     *
     * @param p0 POI搜索结果
     * @param p1 返回码
     */
    override fun onPoiSearched(p0: PoiResult?, p1: Int) {
        if (AMapException.CODE_AMAP_SUCCESS == p1 && p0 != null) {
            val poiList = p0.pois
            if (poiList == null || poiList.isEmpty()) {
                return
            }
            val poiBeanList: ArrayList<POIBean> = ArrayList()
            for (poi in poiList) {
                val poiBean = POIBean(
                        poi.provinceName,
                        poi.provinceCode,
                        poi.cityName,
                        poi.cityCode,
                        poi.adName,
                        poi.adCode,
                        poi.latLonPoint.latitude,
                        poi.latLonPoint.longitude,
                        poi.title,
                        poi.snippet
                )
                poiBeanList.add(poiBean)
            }

            mPoiBeanList.clear()
            mPoiBeanList.addAll(poiBeanList)
            mPOIAdapter.notifyDataSetChanged()
        } else {
            Log.e(TAG, "onPoiSearched error")
        }
    }

    /**
     * 定位结果回调
     *
     * @param p0 定位结果
     */
    override fun onLocationChanged(p0: AMapLocation?) {
        if (p0 != null && p0.errorCode == AMapLocation.LOCATION_SUCCESS) {
            mCurrentCity = p0.city
            mCurrentLatLon = LatLng(p0.latitude, p0.longitude)
            moveMapCamera(mCurrentLatLon)
            doPoiSearch(p0.address, POI_TYPE, mCurrentCity)
        } else {
            Log.e(TAG, "onLocationChanged error")
        }
    }

    /**
     * 地图加载回调
     */
    override fun onMapLoaded() {
        addMarkerInCenter()
    }

    /**
     * 标记点加在中间位置
     */
    private fun addMarkerInCenter() {
        val latLon = mAMap.cameraPosition.target
        val point = mAMap.projection.toScreenLocation(latLon)
        mMarker = mAMap.addMarker(MarkerOptions().anchor(0.5F, 0.5F).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)))
        mMarker.setPositionByPixels(point.x, point.y)
        mMarker.zIndex = 1F
    }

    /**
     * 地图点击回调
     */
    override fun onMapClick(p0: LatLng?) {
        Toast.makeText(this, "点击了地图", Toast.LENGTH_LONG).show()
    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    /**
     * InfoWindow重写
     */
    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    /**
     * 地图点击
     */
    override fun onTouch(p0: MotionEvent?) {
    }

    /**
     * 搜索框提交监听
     *
     * @param query
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    /**
     * 书如变化监听
     *
     * @param newText
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    /**
     * 相机移动完成回调
     */
    override fun onCameraChangeFinish(p0: CameraPosition?) {
    }

    /**
     * 相机移动回调
     */
    override fun onCameraChange(p0: CameraPosition?) {
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.img_location) {
            doLocation()
        }
    }
}