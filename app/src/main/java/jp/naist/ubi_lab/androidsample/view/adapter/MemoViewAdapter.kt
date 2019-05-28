package jp.naist.ubi_lab.androidsample.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.R
import io.realm.Realm
import io.realm.RealmConfiguration




class MemoViewAdapter : BaseAdapter(){
    private val tag = "MemoViewAdapter"
    private lateinit var context: Context
    private lateinit var layoutInflater: LayoutInflater
    lateinit var memoList: ArrayList<MemoData>
    private var mRealm : Realm? = null

    //コンストラクタ
    fun memoViewAdapter(context: Context){
        this.context = context
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    //Viewの設定を行う
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.w(tag, memoList.toString())
        //inflaterでmemo_utilsを読み込み
        val convertView = layoutInflater.inflate(R.layout.memo_utils, parent, false)
        (convertView.findViewById(R.id.title_text) as TextView).text = memoList[position].title
        (convertView.findViewById(R.id.main_text) as TextView).text = memoList[position].main

        val id = memoList[position].id!!
        //削除ボタンの設定
        val deleteButton = convertView.findViewById(R.id.delete_button) as ImageButton
        deleteButton.setOnClickListener { view ->
            (parent as ListView).performItemClick(
                view,
                position,
                id
            )
            Toast.makeText(context, "Memo was deleted", LENGTH_SHORT).show()
        }

        //編集ボタンの設定
        val editButton = convertView.findViewById(R.id.edit_button) as ImageButton
        editButton.setOnClickListener{ view ->
            (parent as ListView).performItemClick(
                view,
                position,
                id
            )
            Toast.makeText(context, "Edit Mode", LENGTH_SHORT).show()
        }

        //メモ欄をクリックした時の設定
        val memoItem = convertView.findViewById(R.id.memo_item) as LinearLayout
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

    //セットしているデータが更新されたときに再レンダリングを要請
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        Log.e(tag,"Item changed")
    }

    //Item本体を返却
    override fun getItem(position: Int): Any {
        return memoList[position]
    }

    //ItemのユニークIdを返却
    override fun getItemId(position: Int): Long {
        return memoList[position].id!!
    }

    //全Itemの数を返却
    override fun getCount(): Int {
        return memoList.size
    }

    //メモを削除する関数
    fun memoDelete(id: Long, position: Int){
        //realm DBから該当するMemoDataを削除
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

        //セットしているリストから削除
        memoList.remove(memoList[position])
        //レンダリングを要求
        notifyDataSetChanged()
        //トーストを表示
        Toast.makeText(context, "Memo was deleted", LENGTH_SHORT).show()
    }
}
