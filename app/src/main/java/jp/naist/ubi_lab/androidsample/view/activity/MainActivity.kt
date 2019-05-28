package jp.naist.ubi_lab.androidsample.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.format.DateFormat
import android.util.Log
import jp.naist.ubi_lab.androidsample.repository.LocationManager
import jp.naist.ubi_lab.androidsample.view.adapter.WeatherViewAdapter
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject
import okhttp3.*
import jp.naist.ubi_lab.androidsample.model.WeatherData
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.R
import jp.naist.ubi_lab.androidsample.view.fragment.SensorFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() , SensorFragment.MyListener{
    override fun onClickButton() {
    }

    private val tag = "MainActivity"
    private val REQUEST_PERMISSION_COARSE_LOCATION = 101
    private var locationManager: LocationManager =
        LocationManager(this)
    private var locationLAT = 0.0
    private var locationLNG = 0.0
    private var res = ""
    private var mRealm : Realm? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        }

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)

        locationManager.start()
        location_button.setOnClickListener {
            getGps()
        }
        api_button.setOnClickListener {
            fetchWeather()
        }
        memo_button.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }
        save_button.setOnClickListener {
            saveMemo()
        }
        sensor_fragment_button.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragment = SensorFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }


    override fun onDestroy() {
        locationManager.stop()
        mRealm!!.close()
        super.onDestroy()
    }


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


    private fun fetchWeather() {
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
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
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

                    // UI反映
                    runOnUiThread {
                        drawWeather(weatherList)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }


    private fun drawWeather(weathers : ArrayList<WeatherData>){
        Log.w(tag, weathers.toString())
        val listView = weather_list
        val weatherViewAdapter = WeatherViewAdapter()
        weatherViewAdapter.weatherViewAdapter(this)
        weatherViewAdapter.weatherList = weathers
        listView.adapter = weatherViewAdapter
        weatherViewAdapter.setListViewHeightBasedOnChildren(listView)
    }

    private fun saveMemo(){
        val idMax = mRealm!!.where(MemoData::class.java).max("id")
        val id: Long = if(idMax!=null) idMax.toLong()+1
        else 0L

        mRealm!!.executeTransaction {
            val memo = mRealm!!.createObject(MemoData::class.java, id)
            memo.title = memo_title_text.text.toString()
            memo.main = memo_main_text.text.toString()
            memo.time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
            Log.e(tag,memo.toString())
            mRealm!!.copyToRealm(memo)
        }
        memo_title_text.text = null
        memo_main_text.text = null
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun isLocationPermissionGranted(): Boolean = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> true // permission 確認の必要なし
        else -> ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() =
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSION_COARSE_LOCATION
        )
}
