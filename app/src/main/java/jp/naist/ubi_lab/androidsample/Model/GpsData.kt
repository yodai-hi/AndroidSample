package jp.naist.ubi_lab.androidsample.Model

import android.location.Location
import android.text.format.DateFormat

data class GpsData(
    var latitude: Double,
    var longitude: Double,
    var got_time: String?,
    var now_time: String?
)
{ constructor():this(135.0, 34.39, null, null)
    fun set(location: Location){
        this.latitude = location.latitude
        this.longitude = location.longitude
        this.got_time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", location.time).toString()
        this.now_time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
    }
}
