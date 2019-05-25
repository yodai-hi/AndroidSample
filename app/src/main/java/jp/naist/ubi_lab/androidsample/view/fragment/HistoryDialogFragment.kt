package jp.naist.ubi_lab.androidsample.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.util.Log
import android.widget.Toast
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.EditText
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.viewmodel.MemoViewAdapter


class HistoryDialogFragment : DialogFragment() {
    private val mTAG = "HistoryDialogFragment"
    private var id: Long? = null
    private var title: String? = null
    private var main: String? = null
    private var time: String? = null
    private var mRealm : Realm? = null
    private var mode: Boolean = true
    private var adapter: MemoViewAdapter? = null



    fun callFromOut(memo: MemoData, mode: Boolean, adapter: MemoViewAdapter) {
        Log.d(mTAG, "callFromOut this method")
        this.id = memo.id
        this.title = memo.title
        this.main = memo.main
        this.time = memo.time
        this.mode = mode
        this.adapter = adapter
    }


    // ダイアログが生成された時に呼ばれるメソッド ※必須
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if(mode){
            createViewDialog()
        }
        else{
            createEditDialog()
        }
    }

    private fun createViewDialog() : Dialog{
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder = AlertDialog.Builder(activity)
        // タイトル設定
        dialogBuilder.setTitle(title)
        // 表示する文章設定
        dialogBuilder.setMessage(main)

        // NGボタン作成
        dialogBuilder.setNegativeButton("Close") { _, _ ->
            // 何もしないで閉じる
        }

        // dialogBulderを返す
        return dialogBuilder.create()
    }

    private fun createEditDialog() : Dialog{

        // テキスト入力用Viewの作成
        val editTitleView = EditText(activity)
        editTitleView.setText(title)
        editTitleView.setLines(1)

        val editMainView = EditText(activity)
        editMainView.setText(main)
        editMainView.setLines(7)

        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化
        val dialogBuilder = AlertDialog.Builder(activity)
        // タイトル設定
        dialogBuilder.setCustomTitle(editTitleView)
        // Viewをカスタマイズ
        dialogBuilder.setView(editMainView)
        // 表示する文章設定
//        dialogBuilder.setMessage(main)

        // OKボタン作成
        dialogBuilder.setPositiveButton("Save"){ _, _ ->
            // トーストを出す
            realmInit()
            Toast.makeText(activity, "Save edited memo", Toast.LENGTH_SHORT).show()
            realmUpdate(id!!, editTitleView.text.toString(), editMainView.text.toString())
            adapter!!.notifyDataSetChanged()
            adapter!!.memoList = readMemo()
            mRealm!!.close()
        }

        // NGボタン作成
        dialogBuilder.setNegativeButton("Cancel"){ _, _ ->
            // 何もしないで閉じる
        }

        // dialogBulderを返す
        return dialogBuilder.create()
    }

    private fun realmInit(){
        Realm.init(context!!)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
    }

    private fun realmUpdate(id:Long, title:String, main:String){
        mRealm!!.executeTransaction {
            val memo = mRealm!!.where(MemoData::class.java).equalTo("id",id).findFirst()
            memo!!.title = title
            memo.main = main
            memo.time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
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
}
