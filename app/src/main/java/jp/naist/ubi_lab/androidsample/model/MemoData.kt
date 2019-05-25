package jp.naist.ubi_lab.androidsample.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MemoData(
//@PrimaryKeyはRealmに保存するときのユニークKEY
    @PrimaryKey open var id : Long?,
    var title : String?,
    var main : String?,
    var time : String?
) : RealmObject(){
    //インスタンスを生成した時のMemoDataの中身
    constructor(): this(null,null, null,null)
    //これを呼び出すことで外部からインスタンスに値を渡せる
    fun set(id:Long, title:String, main: String, time: String){
        this.id = id
        this.title = title
        this.main = main
        this.time = time
    }
}
