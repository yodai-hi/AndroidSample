package jp.naist.ubi_lab.androidsample.view.adapter
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ListView
import jp.naist.ubi_lab.androidsample.model.WeatherData
import android.widget.TextView
import jp.naist.ubi_lab.androidsample.R

class WeatherViewAdapter : BaseAdapter() {
    private lateinit var context: Context
    private lateinit var layoutInflater: LayoutInflater
    lateinit var weatherList: ArrayList<WeatherData>

    //コンストラクタ
    fun weatherViewAdapter(context: Context){
        this.context = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    //Viewの設定を行う
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //inflaterでweather_utilsを読み込み
        val convertView = layoutInflater.inflate(R.layout.weather_utils, parent, false)
        (convertView.findViewById(R.id.time_text) as TextView).text = weatherList[position].time
        (convertView.findViewById(R.id.temp_text_max) as TextView).text = weatherList[position].temperature_max
        (convertView.findViewById(R.id.temp_text_min) as TextView).text = weatherList[position].temperature_min
        (convertView.findViewById(R.id.humi_text) as TextView).text = weatherList[position].humidity

        //雲の量と雨か否かを変数に保存
        val cloud = weatherList[position].clouds!!
        val rain = weatherList[position].rain!!

        //天候に合わせてImageを切り替え
        val imageId = when {
            !rain and (cloud < 60) -> R.drawable.ic_sun_24dp    //雨でなく雲が60%未満
            !rain and (cloud <=100) -> R.drawable.ic_cloud_24dp //雨でなく雲が60%以上100%未満
            else -> R.drawable.ic_umbrella_24dp                 //雨である（それ以外）
        }
        (convertView.findViewById(R.id.weather_image) as ImageView).setImageResource(imageId)

        return convertView
    }

    //Item本体を返却
    override fun getItem(position: Int): Any {
        return weatherList[position]
    }

    //ItemのユニークIdを返却
    override fun getItemId(position: Int): Long {
        return weatherList[position].id!!
    }

    //全Itemの数を返却
    override fun getCount(): Int {
        return weatherList.size
    }

    //ListViewの高さをListの数に合わせて調整
    fun setListViewHeightBasedOnChildren(listView: ListView) {

        val listAdapter = listView.adapter ?: return        //ListAdapterを取得
        var totalHeight = 0        //初期化

        //個々のアイテムの高さを測り、加算していく
        for (i in 0 until listAdapter.count)
        {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        //LayoutParamsを取得
        val params = listView.layoutParams
        //(区切り線の高さ * 要素数の数)だけ足す
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        //LayoutParamsにheightをセット
        listView.layoutParams = params
    }
}
