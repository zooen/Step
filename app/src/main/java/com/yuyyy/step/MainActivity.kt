package com.yuyyy.step

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import android.location.Location

import java.text.DecimalFormat

import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.MyLocationStyle

import com.amap.api.trace.LBSTraceClient
import com.amap.api.trace.TraceListener
import com.amap.api.trace.TraceLocation
import com.amap.api.trace.TraceOverlay
import com.yuyyy.step.record.PathRecord
import com.yuyyy.step.recordutil.Util
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), LocationSource, AMapLocationListener, TraceListener {

    private val CALLTRACE = 0
    private var mMapView: MapView? = null
    private var mAMap: AMap? = null
    //定位需要的数据
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    private var mPolyoptions: PolylineOptions? = null
    private var tracePolytion: PolylineOptions? = null
    private var mpolyline: Polyline? = null
    private var record: PathRecord? = null
    private var mStartTime: Long = 0
    private var mEndTime: Long = 0
    private var btn: ToggleButton? = null
    //private var DbHepler: DbAdapter? = null
    private val mTracelocationlist = ArrayList<TraceLocation>()
    private val mOverlayList = ArrayList<TraceOverlay?>()
    private val recordList = ArrayList<AMapLocation>()
    private val tracesize = 30
    private var mDistance = 0
    private var mTraceoverlay: TraceOverlay? = null
    private var mResultShow: TextView? = null
    private var mlocMarker: Marker? = null

    //定位蓝点
    //private var myLocationStyle: MyLocationStyle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/

        mMapView = findViewById<MapView>(R.id.map)
        mMapView?.onCreate(savedInstanceState)// 此方法必须重写

        init()
        initpolyline()
    }

    /**
     * 初始化AMap对象
     */
    private fun init() {
        if (mAMap == null) {
            mAMap = mMapView?.getMap()
            setUpMap()
        }
        /*btn = findViewById(R.id.locationbtn) as ToggleButton
        btn.setOnClickListener(View.OnClickListener {
            if (btn.isChecked()) {
                mAMap.clear(true)
                if (record != null) {
                    record = null
                }
                record = PathRecord()
                mStartTime = System.currentTimeMillis()
                record.setDate(getcueDate(mStartTime))
                mResultShow.setText("总距离")
            } else {
                mEndTime = System.currentTimeMillis()
                mOverlayList.add(mTraceoverlay)
                val decimalFormat = DecimalFormat("0.0")
                mResultShow.setText(
                        decimalFormat.format(getTotalDistance() / 1000.0) + "KM")
                val mTraceClient = LBSTraceClient(applicationContext)
                mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()), LBSTraceClient.TYPE_AMAP, this@MainActivity)
                saveRecord(record.getPathline(), record.getDate())
            }
        })
        mResultShow = findViewById(R.id.show_all_dis) as TextView*/

        mTraceoverlay = TraceOverlay(mAMap)
    }

    private fun startRecord(){
        mAMap?.clear(true)
        if (record != null) {
            record = null
        }
        record = PathRecord()
        mStartTime = System.currentTimeMillis()
        record?.setDate(getcueDate(mStartTime))
        //mResultShow.setText("总距离")
    }

    private fun stopRecord(){
        mEndTime = System.currentTimeMillis()
        mOverlayList.add(mTraceoverlay)
        val decimalFormat = DecimalFormat("0.0")
        //mResultShow.setText(decimalFormat.format(getTotalDistance() / 1000.0) + "KM")
        val mTraceClient = LBSTraceClient(applicationContext)
        mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record?.getPathline()), LBSTraceClient.TYPE_AMAP, this@MainActivity)
        saveRecord(record?.getPathline(), record?.getDate())
    }

    protected fun saveRecord(list: List<AMapLocation>?, time: String?) {
        if (list != null && list.size > 0) {
            /*DbHepler = DbAdapter(this)
            DbHepler.open()
            val duration = getDuration()
            val distance = getDistance(list)
            val average = getAverage(distance)
            val pathlineSring = getPathLineString(list)
            val firstLocaiton = list[0]
            val lastLocaiton = list[list.size - 1]
            val stratpoint = amapLocationToString(firstLocaiton)
            val endpoint = amapLocationToString(lastLocaiton)
            DbHepler.createrecord(distance.toString(), duration, average,
                    pathlineSring, stratpoint, endpoint, time)
            DbHepler.close()*/
        } else {
            Toast.makeText(this@MainActivity, "没有记录到路径", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun getDuration(): String {
        return ((mEndTime - mStartTime) / 1000f).toString()
    }

    private fun getAverage(distance: Float): String {
        return (distance / (mEndTime - mStartTime).toFloat()).toString()
    }

    private fun getDistance(list: List<AMapLocation>?): Float {
        var distance = 0f
        if (list == null || list.size == 0) {
            return distance
        }
        for (i in 0 until list.size - 1) {
            val firstpoint = list[i]
            val secondpoint = list[i + 1]
            val firstLatLng = LatLng(firstpoint.latitude,
                    firstpoint.longitude)
            val secondLatLng = LatLng(secondpoint.latitude,
                    secondpoint.longitude)
            val betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng).toDouble()
            distance = (distance + betweenDis).toFloat()
        }
        return distance
    }

    private fun getPathLineString(list: List<AMapLocation>?): String {
        if (list == null || list.size == 0) {
            return ""
        }
        val pathline = StringBuffer()
        for (i in list.indices) {
            val location = list[i]
            val locString = amapLocationToString(location)
            pathline.append(locString).append(";")
        }
        var pathLineString = pathline.toString()
        pathLineString = pathLineString.substring(0,
                pathLineString.length - 1)
        return pathLineString
    }

    private fun amapLocationToString(location: AMapLocation): String {
        val locString = StringBuffer()
        locString.append(location.latitude).append(",")
        locString.append(location.longitude).append(",")
        locString.append(location.provider).append(",")
        locString.append(location.time).append(",")
        locString.append(location.speed).append(",")
        locString.append(location.bearing)
        return locString.toString()
    }

    private fun initpolyline() {
        mPolyoptions = PolylineOptions()
        mPolyoptions!!.width(10f)
        mPolyoptions!!.color(Color.GRAY)
        tracePolytion = PolylineOptions()
        tracePolytion!!.width(40f)
        tracePolytion!!.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line))
    }

    /**
     * 设置一些amap的属性
     */
    private fun setUpMap() {

        val myLocationStyle: MyLocationStyle
        myLocationStyle = MyLocationStyle()//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000) //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。

        mAMap?.setMyLocationStyle(myLocationStyle)//设置定位蓝点的Style
        mAMap?.setMyLocationEnabled(true)// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        //设置地图的放缩级别
        mAMap?.moveCamera(CameraUpdateFactory.zoomTo(12f));

        val uiSettings = mAMap?.getUiSettings()
        uiSettings!!.setMyLocationButtonEnabled(true)//设置默认定位按钮是否显示，非必需设置。
        uiSettings!!.setZoomControlsEnabled(false) //关闭放大缩小按钮
        //uiSettings.setCompassEnabled(true);//显示罗盘
        uiSettings.setScaleControlsEnabled(true);//显示比例尺
        uiSettings.setLogoPosition(0);//设置logo位置

        // 设置定位监听
        mAMap?.setLocationSource(this)
        mAMap?.setOnMyLocationChangeListener(object : AMap.OnMyLocationChangeListener {
            override fun onMyLocationChange(location: Location) {
                //从location对象中获取经纬度信息，地址描述信息，建议拿到位置之后调用逆地理编码接口获取
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }*/

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }


    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        mListener = listener
        startlocation()
    }

    override fun deactivate() {
        mListener = null
        if (mLocationClient != null) {
            mLocationClient?.stopLocation()
            mLocationClient?.onDestroy()

        }
        mLocationClient = null
    }

    /**
     * 定位结果回调
     * @param amapLocation 位置信息类
     */
    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.errorCode == 0) {
                mListener?.onLocationChanged(amapLocation)// 显示系统小蓝点
                val mylocation = LatLng(amapLocation.latitude,
                        amapLocation.longitude)
                mAMap?.moveCamera(CameraUpdateFactory.changeLatLng(mylocation))
                //if (btn.isChecked()) {
                    record?.addpoint(amapLocation)
                    mPolyoptions?.add(mylocation)
                    mTracelocationlist.add(Util.parseTraceLocation(amapLocation))
                    redrawline()
                    if (mTracelocationlist.size > tracesize - 1) {
                        trace()
                    }
                //}
            } else {
                val errText = ("定位失败," + amapLocation.errorCode + ": "
                        + amapLocation.errorInfo)
                Log.e("AmapErr", errText)
            }
        }
    }



    /**
     * 开始定位。
     */
    private fun startlocation() {
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            // 设置定位监听
            mLocationClient?.setLocationListener(this)
            // 设置为高精度定位模式
            mLocationOption?.setLocationMode(AMapLocationMode.Hight_Accuracy)

            mLocationOption?.setInterval(2000)

            // 设置定位参数
            mLocationClient?.setLocationOption(mLocationOption)
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient?.startLocation()

        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getcueDate(time: Long): String {
        val formatter = SimpleDateFormat(
                "yyyy-MM-dd  HH:mm:ss ")
        val curDate = Date(time)
        return formatter.format(curDate)
    }

    /**
     * 实时轨迹画线
     */
    private fun redrawline() {
        if (mPolyoptions!!.getPoints().size > 1) {
            if (mpolyline != null) {
                mpolyline!!.setPoints(mPolyoptions!!.getPoints())
            } else {
                mpolyline = mAMap!!.addPolyline(mPolyoptions)
            }
        }
        //		if (mpolyline != null) {
        //			mpolyline.remove();
        //		}
        //		mPolyoptions.visible(true);
        //		mpolyline = mAMap.addPolyline(mPolyoptions);
        //			PolylineOptions newpoly = new PolylineOptions();
        //			mpolyline = mAMap.addPolyline(newpoly.addAll(mPolyoptions.getPoints()));
        //		}
    }

    fun record(view: View) {
        //val intent = Intent(this@MainActivity, RecordActivity::class.java)
        //startActivity(intent)
    }

    private fun trace() {
        val locationList = ArrayList(mTracelocationlist)
        val mTraceClient = LBSTraceClient(applicationContext)
        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this)
        val lastlocation = mTracelocationlist[mTracelocationlist.size - 1]
        mTracelocationlist.clear()
        mTracelocationlist.add(lastlocation)
    }

    /**
     * 轨迹纠偏失败回调。
     * @param i
     * @param s
     */
    override fun onRequestFailed(i: Int, s: String) {
        mOverlayList!!.add(mTraceoverlay)
        mTraceoverlay = TraceOverlay(mAMap)
    }

    override fun onTraceProcessing(i: Int, i1: Int, list: List<LatLng>) {

    }


    /**
     * 轨迹纠偏成功回调。
     * @param lineID 纠偏的线路ID
     * @param linepoints 纠偏结果
     * @param distance 总距离
     * @param waitingtime 等待时间
     */
    override fun onFinished(lineID: Int, linepoints: List<LatLng>?, distance: Int, waitingtime: Int) {
        if (lineID == 1) {
            if (linepoints != null && linepoints.size > 0) {
                mTraceoverlay!!.add(linepoints)
                mDistance += distance
                mTraceoverlay!!.setDistance(mTraceoverlay!!.getDistance() + distance)
                if (mlocMarker == null) {
                    mlocMarker = mAMap!!.addMarker(MarkerOptions().position(linepoints[linepoints.size - 1])
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.point))
                            .title("距离：" + mDistance + "米"))
                    mlocMarker!!.showInfoWindow()
                } else {
                    mlocMarker!!.setTitle("距离：" + mDistance + "米")
                    Toast.makeText(this@MainActivity, "距离" + mDistance, Toast.LENGTH_SHORT).show()
                    mlocMarker!!.setPosition(linepoints[linepoints.size - 1])
                    mlocMarker!!.showInfoWindow()
                }
            }
        } else if (lineID == 2) {
            if (linepoints != null && linepoints.size > 0) {
                mAMap!!.addPolyline(PolylineOptions()
                        .color(Color.RED)
                        .width(40f).addAll(linepoints))
            }
        }

    }

}
