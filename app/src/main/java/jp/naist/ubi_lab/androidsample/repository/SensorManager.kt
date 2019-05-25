package jp.naist.ubi_lab.androidsample.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.text.format.DateFormat
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.SensorData


class SensorManager(context: Context) {

    private val tag = "SensorManager"
    private var accelerationSensorEventListner: AccelerationSensorEventListener = AccelerationSensorEventListener(context)

    fun start() {
        accelerationSensorEventListner.connect()
        Log.w(tag,"----:CONNECT")
    }

    fun stop() {
        accelerationSensorEventListner.disconnect()
        Log.w(tag,"----:DISCONNECT")
    }

    fun getSensorData() {
        Log.w(tag,"----:GET_SENSOR_DATA")
        accelerationSensorEventListner.getSensorData()
    }


    class AccelerationSensorEventListener(private val context: Context): SensorEventListener {

        private var mSensorManager: SensorManager? = null
        private var mRealm: Realm? = null
        private var sensorData: SensorData = SensorData()
        private var acc: FloatArray = arrayOf(0f, 0f, 0f).toFloatArray()
        private var mag: FloatArray = arrayOf(0f, 0f, 0f).toFloatArray()
        private var time: String = ""
        private var dummySensorData: SensorData = SensorData()


        @SuppressLint("ServiceCast")
        fun connect() {
            mSensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
            val acceleration: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            mSensorManager!!.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL)
            val magneticField: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            mSensorManager!!.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL)
            realmInit()
        }

        fun disconnect() {
            if(mSensorManager != null) mSensorManager!!.unregisterListener(this)

        }

        fun getSensorData() {
            val idMax = mRealm!!.where(SensorData::class.java).max("id")
            val id: Long = if(idMax!=null) idMax.toLong()+1
            else 0L
            sensorData.set(id, time, acc, mag)
            dummySensorData.set(id,"", arrayOf(0f, 0f, 0f).toFloatArray(), arrayOf(0f, 0f, 0f).toFloatArray())
            if(sensorData != dummySensorData) {
                mRealm!!.executeTransaction {
                    val sensor = mRealm!!.createObject(SensorData::class.java, id)
                    sensor.time = sensorData.time
                    sensor.acc_x = sensorData.acc_x
                    sensor.acc_y = sensorData.acc_y
                    sensor.acc_z = sensorData.acc_z
                    sensor.mag_x = sensorData.mag_x
                    sensor.mag_y = sensorData.mag_y
                    sensor.mag_z = sensorData.mag_z

                    mRealm!!.copyToRealm(sensor)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
            if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                acc = event.values
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mag = event.values
            }
        }

        private fun realmInit(){
            Realm.init(context)
            val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
            mRealm = Realm.getInstance(realmConfig)
        }
    }
}
