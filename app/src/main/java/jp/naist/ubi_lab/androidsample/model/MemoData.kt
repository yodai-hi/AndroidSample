package jp.naist.ubi_lab.androidsample.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MemoData(
    @PrimaryKey open var id : Long?,
    var title : String?,
    var main : String?,
    open var time : String?
) : RealmObject(){
    constructor(): this(null,null, null,null)
    fun set(id:Long, title:String, main: String, time: String){
        this.id = id
        this.title = title
        this.main = main
        this.time = time
    }
}
