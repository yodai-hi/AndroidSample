package jp.naist.ubi_lab.androidsample.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.format.DateFormat
import android.util.Log
import jp.naist.ubi_lab.androidsample.utils.LocationManager
import jp.naist.ubi_lab.androidsample.viewmodel.WeatherViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject
import okhttp3.*
import jp.naist.ubi_lab.androidsample.model.WeatherData
import android.content.Intent
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.R
import kotlin.collections.ArrayList


class MainActivity : Activity() {

    private val tag = "MainActivity"
    private val REQUEST_PERMISSION_COARSE_LOCATION = 101
    private var locationManager: LocationManager = LocationManager(this)
    private var locationLAT = 0.0
    private var locationLNG = 0.0
    private var res = ""
    private var mRealm : Realm? = null

    //Activityの起動の一番初めに呼ばれる
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //GPSのpermissionを確認
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        }

        //realmの初期化
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)

        locationManager.start()

        //ボタンのクリックリスナー
        location_button.setOnClickListener {
            //GPSを取得
            getGps()
        }
        api_button.setOnClickListener {
            //天気を取得
            fetchWeather()
        }
        memo_button.setOnClickListener {
            //HistoryActivityに遷移
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }
        save_button.setOnClickListener {
            //MemoをRealmに保存
            saveMemo()
        }
    }

    //Activityが破棄されるときに呼ばれる．すべてのインスタンスを初期化しないとメモリリークの原因に
    override fun onDestroy() {
        locationManager.stop()
        mRealm!!.close()
        super.onDestroy()
    }

    //LocationManagerのインスタンスを利用してLocationDataに値を保存
    private fun getGps(){
        val locationData = locationManager.getSensorData()
        location_text_lat.text = String.format("%1$.4f", locationData.latitude)
        location_text_lng.text = String.format("%1$.4f", locationData.longitude)
        location_text_alt.text = String.format("%1$.4f", locationData.altitude)
        location_text_speed.text = locationData.speed.toString()
        location_text_time.text = DateFormat.format("yyyy/MM/dd, kk:mm:ss", locationData.time)
        location_text_time_stamp.text = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis())
        locationLAT = locationData.latitude
        locationLNG = locationData.longitude
    }

    //非同期通信によりAPIをたたいて天気情報を取得
    private fun fetchWeather() {
        //APIのエンドポイントを指定
        val baseURL = "http://api.openweathermap.org/data/2.5/forecast"
        val gpsOption = "?lat=$locationLAT&lon=$locationLNG"
        val modeOption = "&mode=json"
        val cntOption = "&cnt=14"
        val idOption = "&APPID=542ffd081e67f4512b705f89d2a611b2"
        val requestURL = (baseURL + gpsOption + modeOption + cntOption + idOption)

        val request = Request.Builder()
            .url(requestURL)
            .get()
            .build()

        val client = OkHttpClient()

        //結果を表示
        client.newCall(request).enqueue(object : Callback {
            //通信に失敗した場合のコールバック（ソケットが閉じている場合）
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            //通信に成功した場合のコールバック
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //JsonをパースしてWeatherDataクラスに格納
                res = response.body()!!.string()
                val weatherList = ArrayList<WeatherData>()
                try {
                    val resJson = JSONObject(res)
                    val weathers = resJson.getJSONArray("list")
                    for(item in 0 until weathers.length()) {
                        val weather = weathers.getJSONObject(item)
                        val time = weather.getString("dt_txt")
                        val tempMax = String.format("%1$.1f ℃", weather.getJSONObject("main").getDouble("temp_max") / 10 )
                        val tempMin = String.format("%1$.1f ℃", weather.getJSONObject("main").getDouble("temp_min") / 10 )
                        val humi = String.format("%1$.1f ％", weather.getJSONObject("main").getDouble("humidity"))
                        val clouds = weather.getJSONObject("clouds").getInt("all")
                        val rain = weather.getJSONArray("weather").getJSONObject(0).getString("main") == "Rain"
                        val weatherData = WeatherData()
                        weatherData.set(time, tempMax, tempMin, humi, clouds, rain, item.toLong())
                        weatherList.add(weatherData)
                    }

                    //UI反映（非同期処理とは別のUIスレッド上で処理）
                    runOnUiThread {
                        drawWeather(weatherList)
                    }
                    //Jsonのパースに失敗した場合，アプリが落ちないようにcatch
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    //ListViewとAdapterを接続．AdapterにはreadMemo()で撮ってきたデータを挿入．
    private fun drawWeather(weathers : ArrayList<WeatherData>){
        Log.w(tag, weathers.toString())
        val listView = weather_list
        val weatherViewAdapter = WeatherViewAdapter()
        weatherViewAdapter.weatherViewAdapter(this)
        weatherViewAdapter.weatherList = weathers
        listView.adapter = weatherViewAdapter
        weatherViewAdapter.setListViewHeightBasedOnChildren(listView)
    }

    //realmにメモを保存
    private fun saveMemo(){
        //ユニークkeyがかぶらないようにするため，自動インクリメント（Null対策済み）
        val idMax = mRealm!!.where(MemoData::class.java).max("id")
        val id: Long = if(idMax!=null) idMax.toLong()+1
        else 0L

        //realmにデータを保存
        mRealm!!.executeTransaction {
            val memo = mRealm!!.createObject(MemoData::class.java, id)
            memo.title = memo_title_text.text.toString()
            memo.main = memo_main_text.text.toString()
            memo.time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
            Log.e(tag,memo.toString())
            mRealm!!.copyToRealm(memo)
        }

        //Box内のテキストを消去
        memo_title_text.text = null
        memo_main_text.text = null
    }

    //permission未許可の時
    @SuppressLint("ObsoleteSdkInt")
    private fun isLocationPermissionGranted(): Boolean = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> true // permission 確認の必要なし
        else -> ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    //permission許可確認画面を表示
    private fun requestLocationPermission() =
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSION_COARSE_LOCATION
        )
}
