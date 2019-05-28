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

    override fun onResume() {
        super.onResume()
        drawMemo()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        mRealm!!.close()
        super.onDestroy()
    }

    private fun drawMemo(){
        val memoList = readMemo()
        val memoViewAdapter = MemoViewAdapter()
        memoViewAdapter.memoViewAdapter(this)
        memoViewAdapter.memoList = memoList
        memo_list.adapter = memoViewAdapter
        memoViewAdapter.setListViewHeightBasedOnChildren(memo_list)

        memo_list.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, id ->
            when (view.id) {
                R.id.edit_button -> makeDialog(memoList[position], "edit", memoViewAdapter)
                R.id.delete_button -> memoViewAdapter.clickDelete(id, position)
                R.id.memo_item -> makeDialog(memoList[position], "view", memoViewAdapter)
            }
        }
    }

    private fun readMemo() : ArrayList<MemoData> {
        val memoRealm = mRealm!!.where(MemoData::class.java).findAll()
        val memoList = ArrayList<MemoData>()
        memoRealm.forEach {
            val memo = MemoData()
            memo.set(it.id!!, it.title!!, it.main!!, it.time!!)
            memoList.add(memo)
        }

        return memoList
    }

    private fun makeDialog(memo: MemoData, mode: String, adapter: MemoViewAdapter){
        val dialogMode = when(mode){
            "edit" -> false
            else -> true
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.dialog_container, HistoryDialogFragment(), "HistoryDialogFragment")

        // ダイアログを表示する
        val dialogFragment = HistoryDialogFragment()
        dialogFragment.callFromOut(memo, dialogMode, adapter)
        dialogFragment.show(transaction, "test")
    }
}
