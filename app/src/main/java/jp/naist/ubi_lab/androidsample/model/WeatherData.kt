package jp.naist.ubi_lab.androidsample.model

data class WeatherData(
    var id : Long?,
    var time : String?,
    var temperature_max : String?,
    var temperature_min : String?,
    var humidity : String?,
    var clouds : Int?,
    var rain : Boolean?
) {
    //インスタンスを生成した時のMemoDataの中身
    constructor(): this(null,null, null,null,null,null, null)
    //これを呼び出すことで外部からインスタンスに値を渡せる
    fun set(time:String, temp_max: String, temp_min: String, humi: String, clouds: Int, rain: Boolean, id:Long){
        this.id = id
        this.time = time
        this.temperature_max = temp_max
        this.temperature_min = temp_min
        this.humidity = humi
        this.clouds = clouds
        this.rain = rain
    }
}
