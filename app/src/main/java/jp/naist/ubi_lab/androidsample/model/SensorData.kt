package jp.naist.ubi_lab.androidsample.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SensorData (
    @PrimaryKey open var id: Long?,
    var time : String?,
    var acc_x : Float?,
    var acc_y : Float?,
    var acc_z : Float?,
    var mag_x : Float?,
    var mag_y : Float?,
    var mag_z : Float?
    ) : RealmObject() {
        constructor(): this(null, null, null,null,null,null, null,null)
        fun set(id:Long, time:String, acc:FloatArray, mag:FloatArray){
            this.id = id
            this.time = time
            this.acc_x = acc[0]
            this.acc_y = acc[1]
            this.acc_z = acc[2]
            this.mag_x = mag[0]
            this.mag_y = mag[1]
            this.mag_z = mag[2]
        }
}
