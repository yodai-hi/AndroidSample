package jp.naist.ubi_lab.androidsample.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import android.os.Looper
import android.support.v4.app.ActivityCompat
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnSuccessListener
import jp.naist.ubi_lab.androidsample.view.activity.MainActivity


class LocationManager(context: Context) {
    //デバッグ用のタグ
    private val tag = "LocationManager"
    //インスタンスを作製
    private var locationEventListener: GpsLocationEventListener =
        GpsLocationEventListener(context)


    fun start() {
        locationEventListener.connect()
        Log.w(tag,"----:CONNECT")
    }

    fun stop() {
        locationEventListener.disconnect()
        Log.w(tag,"----:DISCONNECT")
    }

    fun getSensorData(): Location {
        Log.w(tag,"----:GET_SENSOR_DATA")
        return locationEventListener.getSensorData()
    }

    //インナークラス．カプセル化（隠蔽）の手段
    class GpsLocationEventListener(private val context: Context){
        //インナークラスのフィールドは外部クラストは完全に切り離されている
        private val tag: String = "LocationEventListener"
        private var isReconnectRequired = false
        private var isStarted = false
        // Fused Location Provider API.
        private var fusedLocationClient: FusedLocationProviderClient? = null
        // Location Settings APIs.
        private var settingsClient: SettingsClient? = null
        private var locationSettingsRequest: LocationSettingsRequest? = null
        private var locationCallback: LocationCallback? = null
        private val locationRequest = LocationRequest()
        private var location: Location? = null

        private var requestingLocationUpdates: Boolean? = null
        private var priority = 1

        //初期化処理
        fun connect() {
            //LocationServiceに関わるクライアント
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            settingsClient = LocationServices.getSettingsClient(context)
            //後に定義．リクエストの精度
            priority = 1

            //コールバックの変数をフィールドに代入
            createLocationCallback()
            //取得の詳細を設定
            createLocationRequest()
            buildLocationSettingsRequest()

            startLocationUpdates()
        }

        //終了処理
        fun disconnect() {
            stopLocationUpdates()
            if (isStarted) {
                Log.e(tag,"----:google_api_client_DISCONNECT")

                isStarted = false
                isReconnectRequired = false
            }
        }

        //locationのコールバックを受け取る
        private fun createLocationCallback() {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    location = locationResult!!.lastLocation
                    Log.w(tag,"----:createLocationCallback:$location")
                }
            }
        }

        //精度，最短インターバルを設定
        private fun createLocationRequest() {
            when (priority) {
                0 -> locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                1 -> locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                2 -> locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
                else -> locationRequest.priority = LocationRequest.PRIORITY_NO_POWER
            }
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 600
        }

        //設定を有効化
        private fun buildLocationSettingsRequest() {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest)
            locationSettingsRequest = builder.build()
        }


        private fun startLocationUpdates() {
            //取得のための設定が存在しているかチェック
            settingsClient!!.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(
                    MainActivity(),
                    OnSuccessListener<LocationSettingsResponse> {
                        Log.w(tag, "---:All location settings are satisfied.")

                        // パーミッションの確認
                        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            !=PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            !=PackageManager.PERMISSION_GRANTED) {

                            return@OnSuccessListener
                        }
                        //ループして継続的にGPS情報を更新する
                        fusedLocationClient!!.requestLocationUpdates(
                            locationRequest, locationCallback, Looper.myLooper())
                    })
            //GPSの起動に成功したフラグ
            requestingLocationUpdates = true
        }

        //GPSを止める
        private fun stopLocationUpdates() {
            if (requestingLocationUpdates!!) {
                return
            }
            fusedLocationClient!!.removeLocationUpdates(locationCallback)
        }

        //locationに入っている値を返す（Null対策済み）
        fun getSensorData(): Location {
            if(location != null) {
                Log.w(tag,"----:RETURN LOCATION")
                return location!!
            }
            return Location("dummy")
        }
    }
}
