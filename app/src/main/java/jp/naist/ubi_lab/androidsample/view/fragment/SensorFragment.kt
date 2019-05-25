package jp.naist.ubi_lab.androidsample.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.naist.ubi_lab.androidsample.R
import jp.naist.ubi_lab.androidsample.repository.SensorManager
import jp.naist.ubi_lab.androidsample.viewmodel.SensorViewAdapter
import kotlinx.android.synthetic.main.fragment_sensor.*
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.SensorData


class SensorFragment : Fragment() {
    private val mTAG: String = "SensorFragment"
    private var sensorManager: SensorManager? = null
    private var mListener: MyListener? = null
    private var mRealm: Realm? = null


    interface MyListener {
        fun onClickButton()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sensor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = SensorManager(context!!)
        sensorManager!!.start()

        realmInit()
        val sensorViewAdapter = SensorViewAdapter()
        sensorViewAdapter.sensorViewAdapter(context!!)
        sensorViewAdapter.sensorList = readSensor()
        sensor_list.adapter = sensorViewAdapter

        sensor_get_button.setOnClickListener {
            sensorManager!!.getSensorData()
            sensorViewAdapter.sensorList = readSensor()
            sensorViewAdapter.notifyDataSetChanged()
        }

        sensor_delete_button.setOnClickListener {
            mRealm!!.executeTransaction {
                val sensor = mRealm!!.where(SensorData::class.java).findAll()
                sensor.forEach{
                        _ -> sensor.deleteFromRealm(0)
                }
                sensorViewAdapter.sensorList = ArrayList()
                sensorViewAdapter.notifyDataSetChanged()
            }
        }

        back_button.setOnClickListener {
            fragmentManager!!.beginTransaction().remove(this).commit()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MyListener) {
            // リスナーをここでセットするようにします
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        // 画面からFragmentが離れたあとに処理が呼ばれることを避けるためにNullで初期化しておく
        mListener = null
    }

    override fun onDestroy() {
        sensorManager!!.stop()
        mRealm!!.close()
        super.onDestroy()
    }


    private fun realmInit(){
        Realm.init(context!!)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
    }


    private fun readSensor(): ArrayList<SensorData> {
        val sensorRealm = mRealm!!.where(SensorData::class.java).findAll()
        val sensorList = ArrayList<SensorData>()
        sensorRealm.forEach {
            val sensor = SensorData()
            sensor.set(
                it.id!!,
                it.time!!,
                arrayOf(it.acc_x!!, it.acc_y!!, it.acc_z!!).toFloatArray(),
                arrayOf(it.mag_x!!, it.mag_y!!, it.mag_z!!).toFloatArray()
            )
            sensorList.add(sensor)
        }
        return sensorList
    }
}
