package com.yuyyy.step.record


import java.util.ArrayList
import com.amap.api.location.AMapLocation


/**
 * 用于记录一条轨迹，包括起点、终点、轨迹中间点、距离、耗时、平均速度、时间
 *
 * @author ligen
 */
class PathRecord {
    private var mStartPoint: AMapLocation? = null
    private var mEndPoint: AMapLocation? = null
    private var mPathLinePoints = ArrayList<AMapLocation>()
    private var mDistance: String? = null
    private var mDuration: String? = null
    private var mAveragespeed: String? = null
    private var mDate: String? = null
    private var mId = 0

    fun PathRecord() {

    }

    fun getId(): Int {
        return mId
    }

    fun setId(id: Int) {
        this.mId = id
    }

    fun getStartpoint(): AMapLocation? {
        return mStartPoint
    }

    fun setStartpoint(startpoint: AMapLocation) {
        this.mStartPoint = startpoint
    }

    fun getEndpoint(): AMapLocation? {
        return mEndPoint
    }

    fun setEndpoint(endpoint: AMapLocation) {
        this.mEndPoint = endpoint
    }

    fun getPathline(): List<AMapLocation> {
        return mPathLinePoints
    }

    fun setPathline(pathline: ArrayList<AMapLocation>) {
        this.mPathLinePoints = pathline
    }

    fun getDistance(): String? {
        return mDistance
    }

    fun setDistance(distance: String) {
        this.mDistance = distance
    }

    fun getDuration(): String? {
        return mDuration
    }

    fun setDuration(duration: String) {
        this.mDuration = duration
    }

    fun getAveragespeed(): String? {
        return mAveragespeed
    }

    fun setAveragespeed(averagespeed: String) {
        this.mAveragespeed = averagespeed
    }

    fun getDate(): String? {
        return mDate
    }

    fun setDate(date: String) {
        this.mDate = date
    }

    fun addpoint(point: AMapLocation) {
        mPathLinePoints.add(point)
    }

    override fun toString(): String {
        val record = StringBuilder()
        record.append("recordSize:" + getPathline().size + ", ")
        record.append("distance:" + getDistance() + "m, ")
        record.append("duration:" + getDuration() + "s")
        return record.toString()
    }
}
