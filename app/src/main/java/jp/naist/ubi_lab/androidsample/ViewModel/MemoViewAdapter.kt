package jp.naist.ubi_lab.androidsample.ViewModel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import jp.naist.ubi_lab.androidsample.Model.MemoData
import jp.naist.ubi_lab.androidsample.R
import io.realm.Realm
import io.realm.RealmConfiguration




class MemoViewAdapter : BaseAdapter(){
    private val tag = "MemoViewAdapter"
    private lateinit var context: Context
    private lateinit var layoutInflater: LayoutInflater
    lateinit var memoList: ArrayList<MemoData>
    private var mRealm : Realm? = null


    fun memoViewAdapter(context: Context){
        this.context = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.w(tag, memoList.toString())
        val convertView = layoutInflater.inflate(R.layout.memo_utils, parent, false)
        (convertView.findViewById(R.id.title_text) as TextView).text = memoList[position].title
        (convertView.findViewById(R.id.main_text) as TextView).text = memoList[position].main

        val id = memoList[position].id!!
        //削除ボタンの設定
        val deleteButton = convertView.findViewById(R.id.delete_button) as ImageButton
        val editButton = convertView.findViewById(R.id.edit_button) as ImageButton
        val memoItem = convertView.findViewById(R.id.memo_item) as LinearLayout

        deleteButton.setOnClickListener { view ->
            (parent as ListView).performItemClick(
                view,
                position,
                id
            )
            Toast.makeText(context, "Memo was deleted", LENGTH_SHORT).show()
        }

        editButton.setOnClickListener{ view ->
            (parent as ListView).performItemClick(
                view,
                position,
                id
            )
            Toast.makeText(context, "Edit Mode", LENGTH_SHORT).show()
        }

        memoItem.setOnClickListener{ view ->
            (parent as ListView).performItemClick(
                view,
                position,
                id
            )
            Toast.makeText(context, "View Mode", LENGTH_SHORT).show()
        }

        return convertView
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        Log.e(tag,"Item changed")
    }

    override fun getItem(position: Int): Any {
        return memoList[position]
    }

    override fun getItemId(position: Int): Long {
        return memoList[position].id!!
    }

    override fun getCount(): Int {
        return memoList.size
    }

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
        val params = listView.layoutParams        //LayoutParamsを取得
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))        //(区切り線の高さ * 要素数の数)だけ足してあげる
        listView.layoutParams = params        //LayoutParamsにheightをセットしてあげる
    }


    fun clickDelete(id: Long, position: Int){
        Realm.init(context)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.w("delete", id.toString())
        mRealm!!.executeTransaction {
            val memo = mRealm!!.where(MemoData::class.java).equalTo("id",id).findAll()
            memo.deleteFromRealm(0)
        }
        mRealm!!.close()
        memoList.remove(memoList[position])
        notifyDataSetChanged()
        Toast.makeText(context, "Memo was deleted", LENGTH_SHORT).show()
    }
}
