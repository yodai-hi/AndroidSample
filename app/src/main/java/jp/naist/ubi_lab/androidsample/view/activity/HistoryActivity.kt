package jp.naist.ubi_lab.androidsample.view.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.view.fragment.HistoryDialogFragment
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.R
import jp.naist.ubi_lab.androidsample.view.adapter.MemoViewAdapter
import kotlinx.android.synthetic.main.activity_history.*
import android.widget.AdapterView


class HistoryActivity : AppCompatActivity() {
    private val mTAG = "HistoryActivity"
    private var mRealm : Realm? = null

    //Activityの起動の一番初めに呼ばれる
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
        drawMemo()
    }

    //Activityが復帰した時に呼ばれる
    override fun onResume() {
        super.onResume()
        drawMemo()
    }

    //戻るボタンの処理
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //Activityが破棄されるときに呼ばれる．すべてのインスタンスを初期化しないとメモリリークの原因に
    override fun onDestroy() {
        mRealm!!.close()
        super.onDestroy()
    }

    //ListViewとAdapterを接続．AdapterにはreadMemo()で撮ってきたデータを挿入．
    private fun drawMemo(){
        val memoList = readMemo()
        val memoViewAdapter = MemoViewAdapter()
        memoViewAdapter.memoViewAdapter(this)
        memoViewAdapter.memoList = memoList
        memo_list.adapter = memoViewAdapter

        //memo_utils内のボタンのクリックリスナー
        memo_list.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, id ->
            when (view.id) {
                //ダイアログをeditモードで呼び出し
                R.id.edit_button -> makeDialog(memoList[position], "edit", memoViewAdapter)
                //memoDelete関数を呼び出し
                R.id.delete_button -> memoViewAdapter.memoDelete(id, position)
                //ダイアログをviewモードで呼び出し
                R.id.memo_item -> makeDialog(memoList[position], "view", memoViewAdapter)
            }
        }
    }

    //realmからMemoDataのDBを呼び出す
    private fun readMemo() : ArrayList<MemoData> {
        val memoRealm = mRealm!!.where(MemoData::class.java).findAll()
        val memoList = ArrayList<MemoData>()
        //DB内にあった全てのデータをMemoData型に変換してmemoListに追加
        memoRealm.forEach {
            val memo = MemoData()
            memo.set(it.id!!, it.title!!, it.main!!, it.time!!)
            memoList.add(memo)
        }
        return memoList
    }

    //Dialogを作製（DialogFragmentを貼る）
    private fun makeDialog(memo: MemoData, mode: String, adapter: MemoViewAdapter){
        val dialogMode = when(mode){
            "edit" -> false
            else -> true
        }
        //fragmentへの遷移
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.dialog_container, HistoryDialogFragment(), "HistoryDialogFragment")

        // ダイアログを表示する
        val dialogFragment = HistoryDialogFragment()
        dialogFragment.historyDialogFragment(memo, dialogMode, adapter)
        dialogFragment.show(transaction, "test")
    }
}
